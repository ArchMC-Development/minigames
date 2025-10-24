package gg.tropic.practice.queue

import gg.scala.cache.uuid.ScalaStoreUuidCache
import gg.scala.commons.ScalaCommons
import gg.scala.commons.agnostic.sync.server.ServerContainer
import gg.scala.commons.agnostic.sync.server.impl.GameServer
import gg.scala.commons.agnostic.sync.server.state.ServerState
import gg.tropic.practice.application.api.defaults.kit.KitDataSync
import gg.tropic.practice.application.api.defaults.map.ImmutableMap
import gg.tropic.practice.application.api.defaults.map.MapDataSync
import gg.tropic.practice.expectation.GameExpectation
import gg.tropic.practice.extensions.formatPlayerPing
import gg.tropic.practice.games.GameState
import gg.tropic.practice.games.duels.DuelRequest
import gg.tropic.practice.games.manager.GameManager
import gg.tropic.practice.games.manager.strategies.ServerSelectionStrategy
import gg.tropic.practice.games.spectate.PlayerSpectateRequest
import gg.tropic.practice.games.spectate.SpectateRequest
import gg.tropic.practice.games.spectate.SpectateResponseStatus
import gg.tropic.practice.games.team.GameTeam
import gg.tropic.practice.games.team.TeamIdentifier
import gg.tropic.practice.kit.feature.FeatureFlag
import gg.tropic.practice.lobbyGroup
import gg.tropic.practice.minigame.MiniGameRPC
import gg.tropic.practice.namespace
import gg.tropic.practice.persistence.RedisShared
import gg.tropic.practice.provider.MiniProviderType
import gg.tropic.practice.provider.MiniProviderVersion
import gg.tropic.practice.queue.variants.BedWarsSubscribableMinigamePlayerQueue
import gg.tropic.practice.queue.variants.EventsSubscribableMinigamePlayerQueue
import gg.tropic.practice.queue.variants.MiniWallsSubscribableMinigamePlayerQueue
import gg.tropic.practice.queue.variants.SkyWarsSubscribableMinigamePlayerQueue
import gg.tropic.practice.queue.variants.robot.SubscribableDuoRobotPlayerQueue
import gg.tropic.practice.queue.variants.robot.SubscribableSoloRobotPlayerQueue
import gg.tropic.practice.region.Region
import gg.tropic.practice.replication.ReplicationResultStatus
import gg.tropic.practice.replication.generation.rpc.GenerationRequirement
import gg.tropic.practice.replications.manager.ReplicationManager
import gg.tropic.practice.serializable.Message
import gg.tropic.practice.suffixWhenDev
import mc.arch.commons.communications.rpc.CommunicationGateway
import mc.arch.minigame.bedwars.neo.BedWarsMode
import mc.arch.minigame.miniwalls.MiniWallsMode
import mc.arch.minigames.microgames.events.EventType
import mc.arch.minigames.skywars.SkyWarsMode
import net.evilblock.cubed.serializers.Serializers
import net.md_5.bungee.api.chat.ClickEvent
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

/**
 * @author GrowlyX
 * @since 9/24/2023
 */
object GameQueueManager
{
    var forceSpecificRegionGames: Region? = null
    private val dpsQueueRedis = CommunicationGateway("gamequeue")

    private val dpsRedisCache = RedisShared.keyValueCache
    private val queueHolder = CentralSubscribablePlayerQueueHolder()

    fun prepareGameFor(
        map: ImmutableMap,
        expectation: GameExpectation,
        region: Region,
        excludeInstance: String? = null,
        version: MiniProviderVersion = MiniProviderVersion.LEGACY,
        selectNewestInstance: Boolean = false
    ): CompletableFuture<Void>
    {
        fun fail(message: String)
        {
            RedisShared.sendMessage(
                expectation.players.toList(),
                listOf(
                    "&cWe were not able to send you to a game server! :(",
                    "&cPlease report the following message to an administrator: &f$message"
                )
            )
        }

        val distinctUsers = expectation.players.distinct()
        if (distinctUsers.size != expectation.players.size)
        {
            fail("Duplicate players are on teams")
            return CompletableFuture.completedFuture(null)
        }

        /**
         * Although we check for the map lock when searching for a random map,
         * we want to handle this edge case for duels and anything else.
         */
        if (map.locked)
        {
            fail("Map service gave an unavailable result for the map")
            return CompletableFuture.completedFuture(null)
        }

        /**
         * At this point, we have a [GameExpectation] that is saved in Redis, and
         * we've gotten rid of the queue entries from the list portion of queue. The players
         * still think they are in the queue, so we can generate the map and THEN update
         * their personal queue status. If they, for some reason, LEAVE the queue at this time, then FUCK ME!
         */
        val serverStatuses = ReplicationManager.allServerStatuses()
        val serverToReplicationMappings = serverStatuses.entries
            .filter {
                val game = ServerContainer
                    .getServer<GameServer?>(it.key)
                    ?: return@filter false

                game.lastHeartbeat + 5000L > System.currentTimeMillis() && game.state == ServerState.Loaded && !game.isDraining()
            }
            .flatMap {
                it.value.status.replications.values.flatten()
            }

        val availableReplication = serverToReplicationMappings
            .sortedBy {
                val game = ServerContainer
                    .getServer<GameServer?>(it.server)
                    ?: return@sortedBy Int.MAX_VALUE

                game.getPlayersCount() ?: Int.MAX_VALUE
            }
            .firstOrNull {
                !it.inUse && it.associatedMapName == map.name &&
                    Region.extractFrom(it.server)
                        .withinScopeOf(forceSpecificRegionGames ?: region)
            }

        // if there's an existing replication to house the game, we can send them directly
        // there. if not, we'll take the server with the least player count
        val serverToRequestReplication = availableReplication?.server
            ?: (ServerSelectionStrategy.select(
                requiredVersion = version,
                requiredType = MiniProviderType.MINIGAME,
                region = region,
                excludeInstance = excludeInstance,
                minigameOrchestratorID = expectation.miniGameConfiguration?.orchestratorID,
                selectNewestInstance = selectNewestInstance,
            )?.id)
            ?: return run {
                fail("No server available to host your minigame")
                CompletableFuture.completedFuture(null)
            }

        return ReplicationManager
            .generateReplication(
                server = serverToRequestReplication,
                map = map.name,
                expectation = expectation,
                requirement = if (availableReplication == null)
                    GenerationRequirement.GENERATE else GenerationRequirement.ALLOCATE
            )
            .thenAcceptAsync {
                if (it.status == ReplicationResultStatus.COMPLETED)
                {
                    RedisShared.redirect(
                        expectation.players.toList(), serverToRequestReplication
                    )
                } else
                {
                    fail("${it.message ?: "N/A (Replication failure)"} (${serverToRequestReplication})")
                }
            }
            .exceptionally {
                it.printStackTrace()
                fail("${it.message ?: "N/A (Replication failure)"} (${serverToRequestReplication})")
                return@exceptionally null
            }

    }

    fun playerIsOnline(uniqueId: UUID) = dpsRedisCache.sync()
        .hexists(
            "symphony:players",
            uniqueId.toString()
        )

    fun load()
    {
        KitDataSync.onReload {
            buildAndValidateQueueIndexes()
        }

        buildAndValidateQueueIndexes()
        dpsRedisCache.sync().del("${namespace().suffixWhenDev()}:duelrequests:*")

        Logger.getGlobal().info("Invalidated existing duel requests")

        val executor = Executors.newScheduledThreadPool(3)
        queueHolder.configure(executor)

        val previouslyQueued = ScalaCommons.bundle().globals().redis()
            .sync()
            .hkeys("$queueV2Namespace:states")
            .map { UUID.fromString(it) }

        ScalaCommons.bundle().globals().redis()
            .sync()
            .del("$queueV2Namespace:states")

        RedisShared.sendMessage(
            previouslyQueued,
            Message()
                .withMessage("&c&lYou were removed from the queue as the system has restarted.")
        )

        Runtime.getRuntime().addShutdownHook(Thread {
            println("Terminating all duel request invalidators before shutdown")
            executor.shutdownNow()
        })

        dpsQueueRedis.configure {
            listen("force-specific-region") {
                val regionID = retrieve<String>("region-id")
                forceSpecificRegionGames = if (regionID != "__RESET__")
                {
                    Region.valueOf(regionID)
                } else
                {
                    null
                }
            }

            val futureMappings = mutableMapOf<String, ScheduledFuture<*>>()
            listen("accept-duel") {
                val request = retrieve<DuelRequest>("request")

                val key = "${namespace().suffixWhenDev()}:duelrequests:${request.requester}:${request.kitID}"
                futureMappings[key]?.cancel(true)

                dpsRedisCache.sync().hdel(key, request.requestee.toString())

                if (!playerIsOnline(request.requester))
                {
                    RedisShared.sendMessage(
                        listOf(request.requestee),
                        listOf("&cThe player that sent you the duel request is no longer online!")
                    )
                    return@listen
                }

                val model = ServerContainer
                    .allServers<GameServer>()
                    .firstOrNull {
                        it.getMetadataValue<List<String>>(
                            "server", "online-list"
                        )!!.contains(
                            request.requester.toString()
                        )
                    }

                if (model == null || lobbyGroup().suffixWhenDev() !in model.groups)
                {
                    RedisShared.sendMessage(
                        listOf(request.requestee),
                        listOf("&cThe player that sent you the duel request is no longer on a practice lobby!")
                    )
                    return@listen
                }

                if (queueHolder.queueOfPlayer(request.requester) != null)
                {
                    RedisShared.sendMessage(
                        listOf(request.requestee),
                        listOf("&cThe player that sent you the duel request is currently queued for a game!")
                    )
                    return@listen
                }

                GameManager.allGames()
                    .firstOrNull { ref -> request.requester in ref.players }
                    .let {
                        if (it != null)
                        {
                            RedisShared.sendMessage(
                                listOf(request.requestee),
                                listOf("&cThe player that sent you the duel request is currently in a game!")
                            )
                            return@let
                        }

                        val kit = KitDataSync.cached().kits[request.kitID]
                            ?: return@let run {
                                RedisShared.sendMessage(
                                    listOf(request.requestee),
                                    listOf(
                                        "&cThe kit you received a duel request for no longer exists!"
                                    )
                                )
                            }

                        // we need to do the check again, so why not
                        val map = if (request.mapID == null)
                        {
                            MapDataSync
                                .selectRandomMapCompatibleWith(kit)
                        } else
                        {
                            MapDataSync.cached().maps[request.mapID]
                        } ?: return@let run {
                            RedisShared.sendMessage(
                                listOf(request.requestee),
                                listOf(
                                    "&cWe found no map compatible with the kit you received a duel request for!"
                                )
                            )
                        }

                        prepareGameFor(
                            map = map,
                            expectation = GameExpectation(
                                players = listOf(request.requester, request.requestee).toMutableSet(),
                                identifier = UUID.randomUUID(),
                                teams = setOf(
                                    GameTeam(teamIdentifier = TeamIdentifier.A, mutableSetOf(request.requester)),
                                    GameTeam(teamIdentifier = TeamIdentifier.B, mutableSetOf(request.requestee))
                                ),
                                kitId = request.kitID,
                                mapId = map.name,
                                configuration = request.configuration
                            ),
                            region = request.region
                        )
                    }
            }

            listen("create-match") {
                val config = retrieve<GameExpectation>("config")
                val mapID = retrieveNullable<String>("map")
                val kitID = retrieve<String>("kit")
                val region = Region.valueOf(retrieve<String>("region"))

                val kit = KitDataSync.cached().kits[kitID]
                    ?: return@listen

                // we need to do the check again, so why not
                val map = if (mapID == null)
                {
                    MapDataSync
                        .selectRandomMapCompatibleWith(kit)
                } else
                {
                    MapDataSync.cached().maps[mapID]
                } ?: return@listen

                prepareGameFor(
                    map = map,
                    expectation = config,
                    region = region
                )
            }

            listen("request-duel") {
                val request = retrieve<DuelRequest>("request")
                val key = "${namespace().suffixWhenDev()}:duelrequests:${request.requester}:${request.kitID}"
                dpsRedisCache.sync().hset(
                    key,
                    request.requestee.toString(),
                    Serializers.gson.toJson(request)
                )

                val kit = KitDataSync.cached().kits[request.kitID]!!
                val map = if (request.mapID != null)
                {
                    MapDataSync.cached().maps[request.mapID]
                } else null

                val requesterName = ScalaStoreUuidCache.username(request.requester)
                val requesterRegion = request.region

                val pingColor = formatPlayerPing(request.requesterPing)

                RedisShared.sendMessage(
                    listOf(request.requestee),
                    Message()
                        .withMessage(
                            " ",
                            "{primary}Duel Request:",
                            "&7┃ &fFrom: {primary}$requesterName &7(${pingColor}${request.requesterPing}ms&7)",
                            "&7┃ &fKit: {primary}${kit.displayName}",
                            "&7┃ &fMap: {primary}${map?.displayName ?: "Random"}",
                            "&7┃ &fRegion: {primary}$requesterRegion",
                            " "
                        )
                        .withMessage(
                            "&a(Click to accept)"
                        )
                        .andCommandOf(
                            ClickEvent.Action.RUN_COMMAND,
                            "/accept $requesterName ${kit.id}"
                        )
                        .andHoverOf("Click to accept!")
                        .withMessage("")
                )

                RedisShared.sendNotificationSound(
                    listOf(request.requestee),
                    "duel-sounds"
                )

                futureMappings[key] = executor.schedule({
                    RedisShared.sendMessage(
                        listOf(request.requestee),
                        listOf("&cYour duel request from &f${requesterName}&c with kit &f${kit.displayName}&c has expired!")
                    )

                    dpsRedisCache.sync().hdel(key, request.requestee.toString())
                }, 1L, TimeUnit.MINUTES)
            }

            listen("spectate") {
                val request = retrieve<PlayerSpectateRequest>("request")

                GameManager.allGames()
                    .firstOrNull { ref -> request.target in ref.players }
                    .let {
                        if (it == null)
                        {
                            RedisShared.sendMessage(
                                listOf(request.player),
                                listOf("&cThe player you tried to spectate is not in a game!")
                            )
                            return@let
                        }

                        if (it.state == GameState.Waiting || it.state == GameState.Starting)
                        {
                            RedisShared.sendMessage(
                                listOf(request.player),
                                listOf("&cThe game has not started yet!")
                            )
                            return@let
                        }

                        if (!it.majorityAllowsSpectators && !request.bypassesSpectatorAllowanceChecks)
                        {
                            RedisShared.sendMessage(
                                listOf(request.player),
                                listOf("&cThe game you tried to spectate has spectators disabled!")
                            )
                            return@let
                        }

                        if (it.miniGameType != null && !request.bypassesSpectatorAllowanceChecks)
                        {
                            RedisShared.sendMessage(
                                listOf(request.player),
                                listOf("&cThis minigame match cannot be spectated by players.")
                            )
                            return@let
                        }

                        MiniGameRPC.spectateService
                            .call(SpectateRequest(
                                server = it.server,
                                gameId = it.uniqueId,
                                player = request.player,
                                target = request.target,
                                bypassesSpectatorAllowanceChecks = request.bypassesSpectatorAllowanceChecks
                            ))
                            .whenComplete { response, throwable ->
                                if (throwable != null || response.status != SpectateResponseStatus.SUCCESS)
                                {
                                    RedisShared.sendMessage(
                                        listOf(request.player),
                                        listOf("&cWe weren't able to add you as a spectator. (${response?.status ?: "N/A"})")
                                    )
                                    return@whenComplete
                                }

                                RedisShared.redirect(
                                    listOf(request.player),
                                    it.server
                                )
                            }
                    }
            }

            listen("join") {
                val entry = retrieve<QueueEntry>("entry")

                val kit = retrieve<String>("kit")
                val queueType = retrieve<QueueType>("queueType")
                val teamSize = retrieve<Int>("teamSize")

                val queueId = "$kit:${queueType.name}:${teamSize}v${teamSize}"
                queueHolder.subscribe(queueId, entry)
            }

            listen("leave") {
                val leader = retrieve<UUID>("leader")
                queueHolder.unsubscribe(leader)
            }
        }
    }

    private fun buildAndValidateQueueIndexes()
    {
        KitDataSync.cached().kits.values
            .forEach { kit ->
                val sizeModels = kit
                    .featureConfig(
                        FeatureFlag.QueueSizes,
                        key = "sizes"
                    )
                    .split(",")
                    .map { sizeModel ->
                        val split = sizeModel.split(":")
                        split[0].toInt() to (split.getOrNull(1)
                            ?.split("+")
                            ?.map(QueueType::valueOf)
                            ?: listOf(QueueType.Casual))
                    }

                QueueType.entries
                    .forEach scope@{
                        for (model in sizeModels)
                        {
                            val queueId = queueId {
                                queueType(it)
                                kit(kit.id)
                                teamSize(model.first)
                            }

                            if (
                                it == QueueType.Ranked &&
                                (!kit.features(FeatureFlag.Ranked) || QueueType.Ranked !in model.second)
                            )
                            {
                                // a ranked queue exists for this kit, but the kit no longer supports ranked
                                queueHolder.forgetPlayerQueue(queueId)
                                return@scope
                            }

                            val queue = if (it != QueueType.Robot)
                            {
                                SubscribableDuelPlayerQueue(
                                    kit = kit,
                                    queueType = it,
                                    teamSize = model.first
                                )
                            } else
                            {
                                SubscribableSoloRobotPlayerQueue(kit, 1)
                            }

                            if (!queueHolder.isHolding(queueId))
                            {
                                queueHolder.trackPlayerQueue(queue)
                            }

                            if (queue is SubscribableSoloRobotPlayerQueue)
                            {
                                val additionalQueue = SubscribableDuoRobotPlayerQueue(kit, 2)
                                if (!queueHolder.isHolding(additionalQueue.id))
                                {
                                    queueHolder.trackPlayerQueue(additionalQueue)
                                }
                            }
                        }
                    }
            }

        dpsQueueRedis.start()

        val bedwarsKitIDs = listOf(
            "bw_mini_solo" to BedWarsMode.Solo,
            "bw_mini_duos" to BedWarsMode.Duo,
            "bw_mega_quads" to BedWarsMode.Quads,
            "bw_mega_trios" to BedWarsMode.Trios,
            "bw_special_4v4" to BedWarsMode.Special4v4,
        )

        bedwarsKitIDs.forEach {
            val bedWarsKit = KitDataSync.cached().kits[it.first]
            if (bedWarsKit != null)
            {
                queueHolder.trackPlayerQueue(BedWarsSubscribableMinigamePlayerQueue(bedWarsKit, it.second))
            } else
            {
                queueHolder.forgetPlayerQueue(it.first)
            }
        }

        val mappings = listOf(
            "sumoevent" to EventType.SUMO
        )

        mappings.forEach {
            val eventKit = KitDataSync.cached().kits[it.first]
            if (eventKit != null)
            {
                queueHolder.trackPlayerQueue(EventsSubscribableMinigamePlayerQueue(eventKit, it.second))
            } else
            {
                queueHolder.forgetPlayerQueue(it.first)
            }
        }

        val skywarsKitIDs = listOf(
            SkyWarsMode.MINI to "sw_mini",
            SkyWarsMode.MINI_MODERN to "modern_sw_mini",
            SkyWarsMode.RANKED to "sw_mini"
        )

        skywarsKitIDs.forEach {
            val skyWarsKit = KitDataSync.cached().kits[it.second]
            if (skyWarsKit != null)
            {
                queueHolder.trackPlayerQueue(SkyWarsSubscribableMinigamePlayerQueue(skyWarsKit, it.first))
            } else
            {
                queueHolder.forgetPlayerQueue(it.second)
            }
        }

        val kitId = MiniWallsMode.MAIN to "mw_main"
        val miniWallsKit = KitDataSync.cached().kits[kitId.second]
        if (miniWallsKit != null)
        {
            queueHolder.trackPlayerQueue(MiniWallsSubscribableMinigamePlayerQueue(miniWallsKit, kitId.first))
        } else
        {
            queueHolder.forgetPlayerQueue(kitId.second)
        }

        // cleanup queues for kits that no longer exist
        queueHolder.playerQueues.forEach { (key, queue) ->
            val kitId = queue.toQueueIDComponents()?.kitID
                ?: return@forEach

            if (KitDataSync.cached().kits[kitId] == null)
            {
                queueHolder.forgetPlayerQueue(key)
            }
        }
    }
}
