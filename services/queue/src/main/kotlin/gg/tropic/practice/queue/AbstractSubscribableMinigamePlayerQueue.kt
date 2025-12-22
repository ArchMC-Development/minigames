package gg.tropic.practice.queue

import gg.tropic.practice.application.api.defaults.kit.ImmutableKit
import gg.tropic.practice.application.api.defaults.map.MapDataSync
import gg.tropic.practice.expectation.GameExpectation
import gg.tropic.practice.games.GameState
import gg.tropic.practice.games.manager.GameManager
import gg.tropic.practice.games.matchmaking.JoinIntoGameRequest
import gg.tropic.practice.games.matchmaking.JoinIntoGameStatus
import gg.tropic.practice.games.matchmaking.MatchmakingMetadata
import gg.tropic.practice.games.team.GameTeam
import gg.tropic.practice.games.team.TeamIdentifier
import gg.tropic.practice.minigame.MiniGameConfiguration
import gg.tropic.practice.minigame.MiniGameMode
import gg.tropic.practice.minigame.MiniGameRPC
import gg.tropic.practice.persistence.RedisShared
import gg.tropic.practice.provider.MiniProviderVersion
import gg.tropic.practice.region.Region
import java.util.UUID

/**
 * @author Subham
 * @since 6/15/25
 */
abstract class AbstractSubscribableMinigamePlayerQueue(
    private val miniGameMode: MiniGameMode,
    private val kit: ImmutableKit,
    private val queueType: QueueType,
    private val selectNewestInstance: Boolean = false,
    private val teamSize: Int = miniGameMode.teamSize,
    private val miniProvider: MiniProviderVersion = MiniProviderVersion.LEGACY,
    override val id: String = queueId {
        kit(kit.id)
        queueType(queueType)
        teamSize(teamSize)
    },
    override val internalQueue: InternalQueue<QueueEntry> = InternalQueue()
) : SubscribablePlayerQueue
{
    abstract fun constructConfigurationForInitiatorEntry(entry: QueueEntry): MiniGameConfiguration
    override fun onProcess(): List<QueueEntry>
    {
        if (internalQueue.isEmpty())
        {
            return emptyList()
        }

        val targetEntry = playersInQueue().first()
        val preferredRegion = if (targetEntry.data.preferredQueueRegion == Region.Both)
            Region.NA else targetEntry.data.preferredQueueRegion

        // Private games always create new instances - skip joining existing games
        val isPrivateGame = targetEntry.data.miniGameQueueConfiguration?.isPrivateGame == true
        
        val existingGameRequiringPlayers = if (isPrivateGame) null else GameManager.allGames()
            .filter {
                var conditions = it.queueId == id &&
                    (it.state == GameState.Waiting || it.state == GameState.Starting)

                if (targetEntry.data.miniGameQueueConfiguration != null)
                {
                    if (targetEntry.data.miniGameQueueConfiguration!!.requiredMapID != null)
                    {
                        // Ensure this existing game has a map
                        conditions = conditions &&
                            it.mapID == targetEntry.data.miniGameQueueConfiguration!!.requiredMapID
                    }

                    if (targetEntry.data.miniGameQueueConfiguration!!.bracket != null)
                    {
                        conditions = conditions &&
                            it.metadata?.bracket == targetEntry.data.miniGameQueueConfiguration!!.bracket
                    }

                    if (targetEntry.data.miniGameQueueConfiguration!!.excludeMiniInstance != null)
                    {
                        conditions = conditions &&
                            it.server != targetEntry.data.miniGameQueueConfiguration!!.excludeMiniInstance
                    }
                }

                return@filter conditions
            }
            .filter { it.players.size + targetEntry.data.players.size <= miniGameMode.maxPlayers() }
            .maxByOrNull { it.players.size }

        val map = targetEntry.data.miniGameQueueConfiguration?.requiredMapID
            ?.let { MapDataSync.cached().maps[it] }
            ?: MapDataSync
                .selectRandomMapCompatibleWith(kit)
            ?: return run {
                RedisShared.sendMessage(
                    targetEntry.data.players,
                    listOf(
                        "&cWe found no map compatible with the kit you are queueing for!"
                    )
                )

                listOf(targetEntry.data)
            }

        if (existingGameRequiringPlayers != null)
        {
            val joinGameResult = runCatching {
                MiniGameRPC.joinIntoGameService
                    .call(
                        JoinIntoGameRequest(
                            server = existingGameRequiringPlayers.server,
                            players = targetEntry.data.players.toSet(),
                            game = existingGameRequiringPlayers
                        )
                    )
                    .join()
            }.getOrElse {
                JoinIntoGameStatus.FAILED_RPC_FAILURE
            }

            if (joinGameResult?.status == JoinIntoGameStatus.SUCCESS)
            {
                RedisShared.redirect(
                    targetEntry.data.players,
                    existingGameRequiringPlayers.server
                )
                return listOf(targetEntry.data)
            } else
            {
                println("Failed to join into game for ${targetEntry.data.leader} (${joinGameResult?.status})")
            }
        }

        val playersToTake = targetEntry.data.players.toMutableSet()
        val teams = TeamIdentifier.ID.values
            .take(miniGameMode.teamCount)
            .mapIndexed { index, identifier ->
                if (playersToTake.isEmpty())
                {
                    return@mapIndexed GameTeam(identifier, mutableSetOf())
                }

                val amount = playersToTake.take(teamSize)
                playersToTake.removeAll(amount)

                return@mapIndexed GameTeam(identifier, amount.toMutableSet())
            }

        if (playersToTake.isNotEmpty())
        {
            RedisShared.sendMessage(
                targetEntry.data.players,
                listOf(
                    "&cWe were unable to fit your party of players into a game!"
                )
            )

            return listOf(targetEntry.data)
        }

        val expectation = GameExpectation(
            identifier = UUID.randomUUID(),
            players = targetEntry.data.players.toMutableSet(),
            teams = teams.toSet(),
            kitId = kit.id,
            mapId = map.name,
            queueType = queueType,
            queueId = id,
            matchmakingMetadataAPIV2 = MatchmakingMetadata(
                region = Region.NA,
                bracket = targetEntry.data.miniGameQueueConfiguration?.bracket
            ),
            miniGameConfiguration = constructConfigurationForInitiatorEntry(targetEntry.data),
            isPrivateGame = isPrivateGame,
            privateGameSettings = targetEntry.data.miniGameQueueConfiguration?.privateGameSettings
        )

        GameQueueManager
            .prepareGameFor(
                map = map,
                expectation = expectation,
                // prefer NA servers if queuing globally
                region = preferredRegion,
                excludeInstance = targetEntry.data.miniGameQueueConfiguration?.excludeMiniInstance,
                selectNewestInstance = selectNewestInstance,
                version = miniProvider
            )
            .exceptionally {
                it.printStackTrace()
                return@exceptionally null
            }

        return listOf(targetEntry.data)
    }
}
