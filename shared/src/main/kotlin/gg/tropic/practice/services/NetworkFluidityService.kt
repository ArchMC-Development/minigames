package gg.tropic.practice.services

import com.google.gson.reflect.TypeToken
import gg.scala.commons.ScalaCommons
import gg.scala.commons.agnostic.sync.ServerSync
import gg.scala.commons.agnostic.sync.server.ServerContainer
import gg.scala.commons.agnostic.sync.server.impl.GameServer
import gg.scala.commons.annotations.commands.customizer.CommandManagerCustomizer
import gg.scala.commons.command.ScalaCommandManager
import gg.scala.commons.playerstatus.isVirtuallyInvisibleToSomeExtent
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.command.ListCommand
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.spatial.tablist.RedisTabSync
import gg.scala.lemon.player.spatial.tablist.events.TabSyncSubscribeEvent
import gg.scala.lemon.util.QuickAccess
import gg.tropic.practice.MinigamesAgentPlugin
import gg.tropic.practice.namespace
import gg.tropic.practice.practiceGroup
import gg.tropic.practice.suffixWhenDev
import me.lucko.helper.Events
import me.lucko.helper.Schedulers
import me.lucko.helper.terminable.composite.CompositeTerminable
import me.lucko.helper.utils.Players
import net.evilblock.cubed.ScalaCommonsSpigot
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.services.CommonsServiceExecutor
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerChatTabCompleteEvent
import java.time.Duration
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.text.toIntOrNull

/**
 * @author GrowlyX
 * @since 12/17/2023
 */
@Service
object NetworkFluidityService
{
    private var playerIDs = setOf<String>()

    data class OnlinePracticePlayer(
        val username: String,
        val rankWeight: Int,
        val displayName: String
    )

    private var localModelCache = listOf<OnlinePracticePlayer>()
    var playerList = ListCommand.PlayerList(0, listOf())

    private lateinit var redisTabSync: RedisTabSync

    private val fluidityTerminable = CompositeTerminable.create()
    private var updater: ScheduledFuture<*>? = null
    private var playerProvider: (() -> Collection<Player>)? = null

    var globalPlayerCount = 0

    fun provideLocalDuelsPlayers(provider: () -> Collection<Player>)
    {
        this.playerProvider = provider
    }

    @Configure
    fun configure()
    {
        if (Bukkit.getPluginManager().getPlugin("Minigames") is MinigamesAgentPlugin)
        {
            return
        }

        val typeToken = object : TypeToken<List<OnlinePracticePlayer>>()
        {}.type

        Schedulers
            .async()
            .runRepeating(Runnable {
                globalPlayerCount = ScalaCommonsSpigot
                    .instance.kvConnection
                    .sync().hgetall(
                        "symphony:instances"
                    )
                    .filter { pair ->
                        System.currentTimeMillis() - (ScalaCommons.bundle().globals().redis()
                            .sync()
                            .hget("symphony:heartbeats", pair.key)
                            ?.toLongOrNull() ?: 0) < Duration
                            .ofSeconds(5L)
                            .toMillis()
                    }
                    .values
                    .sumOf { it.toIntOrNull() ?: 0 }
            }, 0L, 10L)

        redisTabSync = RedisTabSync(syncGroup = if (ServerSync.local.groups.contains("hub")) "hub" else practiceGroup())
        redisTabSync.configure()

        fluidityTerminable.with {
            updater?.cancel(true)
        }

        updater = CommonsServiceExecutor
            .scheduleAtFixedRate({
                val localPlayers = (playerProvider?.invoke() ?: Players.all())
                    .filterNot {
                        it.isVirtuallyInvisibleToSomeExtent()
                    }
                    .map {
                        val lemonPlayer = PlayerHandler.find(it.uniqueId)
                            ?: return@map OnlinePracticePlayer(
                                username = it.name,
                                rankWeight = 0,
                                displayName = "${CC.GRAY}${it.name}"
                            )

                        val rank = QuickAccess.realRank(it)
                        return@map OnlinePracticePlayer(
                            username = it.name,
                            rankWeight = rank.weight,
                            displayName = lemonPlayer.getColoredName()
                        )
                    }

                ScalaCommons.bundle().globals().redis().sync()
                    .setex(
                        "${namespace()}:instances:${ServerSync.local.id}",
                        1L,
                        Serializers.gson.toJson(localPlayers)
                    )

                localModelCache = ServerContainer.getServersInGroup(practiceGroup())
                    .mapNotNull {
                        ScalaCommons.bundle().globals().redis()
                            .sync()
                            .get(
                                "${namespace()}:instances:${it.id}"
                            )
                            ?.let { serialized ->
                                Serializers.gson.fromJson<List<OnlinePracticePlayer>>(serialized, typeToken)
                            }
                    }
                    .flatten()

                playerIDs = localModelCache.map { it.username }.toSet()

                val maxPlayerCount = ServerContainer
                    .getServersInGroupCasted<GameServer>(practiceGroup().suffixWhenDev())
                    .sumOf { it.getMaxPlayers()!! }

                playerList = ListCommand.PlayerList(
                    maxCount = maxPlayerCount,
                    sortedPlayerEntries = localModelCache
                        .sortedByDescending(OnlinePracticePlayer::rankWeight)
                        .map(OnlinePracticePlayer::displayName)
                )
            }, 0L, 500L, TimeUnit.MILLISECONDS)

        Events
            .subscribe(PlayerChatTabCompleteEvent::class.java)
            .handler { event ->
                (playerProvider?.invoke()?.map(Player::getName)
                    .apply {
                        event.tabCompletions.clear()
                    }
                    ?: playerIDs)
                    .toSet()
                    .filter { it.startsWith(event.lastToken, true) }
                    .forEach {
                        if (it !in event.tabCompletions)
                        {
                            event.tabCompletions += it
                        }
                    }
            }
            .bindWith(fluidityTerminable)
    }

    private var isFluidityDisabled = false
    fun disableFluidity()
    {
        if (Bukkit.getPluginManager().getPlugin("Minigames") is MinigamesAgentPlugin)
        {
            return
        }

        isFluidityDisabled = true
        fluidityTerminable.closeAndReportException()
        redisTabSync.closeAndReportException()

        Events
            .subscribe(TabSyncSubscribeEvent::class.java)
            .handler {
                it.isCancelled = true
            }
    }

    fun disableFluidityPartially()
    {
        if (Bukkit.getPluginManager().getPlugin("Minigames") is MinigamesAgentPlugin)
        {
            return
        }

        isFluidityDisabled = true
        fluidityTerminable.closeAndReportException()
    }

    @CommandManagerCustomizer
    fun customize(manager: ScalaCommandManager)
    {
        if (Bukkit.getPluginManager().getPlugin("Minigames") is MinigamesAgentPlugin)
        {
            return
        }

        manager.commandCompletions.registerCompletion("mip-players") {
            if (isFluidityDisabled)
            {
                return@registerCompletion Players.all().map(Player::getName)
            }

            return@registerCompletion playerIDs
        }
    }
}
