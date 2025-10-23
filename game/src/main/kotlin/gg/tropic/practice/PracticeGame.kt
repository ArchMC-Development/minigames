package gg.tropic.practice

import gg.scala.basics.plugin.profile.BasicsProfileService
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.agnostic.sync.ServerSync
import gg.scala.commons.annotations.container.ContainerEnable
import gg.scala.commons.core.plugin.*
import gg.scala.commons.preconfigure.PreConfigureSubTypeProcessor
import gg.scala.lemon.channel.ChatChannelService
import gg.scala.lemon.redirection.aggregate.ServerAggregateHandler
import gg.scala.lemon.redirection.aggregate.impl.LeastTrafficServerAggregateHandler
import gg.tropic.practice.games.GameService
import gg.tropic.practice.minigame.*
import gg.tropic.practice.settings.ChatVisibility
import gg.tropic.practice.settings.DuelsSettingCategory
import gg.tropic.practice.schematics.manipulation.BlockChanger
import gg.tropic.practice.ugc.toHostedWorld
import org.bukkit.Bukkit

/**
 * @author GrowlyX
 * @since 8/4/2022
 */
@Plugin(
    name = "Minigames",
    version = "%remote%/%branch%/%id%",
    description = "Game"
)
@PluginAuthor("ArchMC")
@PluginWebsite("https://arch.mc")
@PluginDependencyComposite(
    PluginDependency("scala-commons"),
    PluginDependency("Lemon"),
    PluginDependency("CoreGameExtensions"),
    PluginDependency("ScBasics"),
    PluginDependency("Parties"),
    // Legacy
    PluginDependency("SlimeWorldManager", soft = true),
    // Modern
    PluginDependency("SlimeWorldPlugin", soft = true),
    PluginDependency("ASPaperPlugin", soft = true),
    PluginDependency("cloudsync", soft = true),
    PluginDependency("Friends", soft = true),
    PluginDependency("ScStaff", soft = true),
    PluginDependency("Apollo-Bukkit", soft = true)
)
class PracticeGame : ExtendedScalaPlugin()
{
    init
    {
        PracticeShared
    }

    @ContainerEnable
    fun containerEnable()
    {
        devProvider = {
            "mipgamedev" in ServerSync.getLocalGameServer().groups
        }

        BlockChanger.load(this, false)

        MiniGameSerializers.configure()
        PreConfigureSubTypeProcessor.register<BasicMiniGameOrchestrator<*>> {
            MiniGameRegistry.miniGameOrchestrators[it.id] = it
        }

        ChatChannelService.default
            .displayToPlayer { player, viewer ->
                val chatVisibility = BasicsProfileService.find(viewer)
                    ?.setting(
                        "${DuelsSettingCategory.DUEL_SETTING_PREFIX}:chat-visibility",
                        ChatVisibility.Global
                    )
                    ?: ChatVisibility.Global

                val bukkitPlayer = Bukkit.getPlayer(player)
                val game = GameService.byPlayerOrSpectator(viewer.uniqueId)
                if (
                    bukkitPlayer != null &&
                    bukkitPlayer.toHostedWorld() != null ||
                    game is AbstractMiniGameGameImpl<*>
                )
                {
                    return@displayToPlayer bukkitPlayer != null && bukkitPlayer.world.name == viewer.world.name
                }

                when (chatVisibility)
                {
                    ChatVisibility.Global -> true
                    else -> bukkitPlayer != null && bukkitPlayer.world.name == viewer.world.name
                }
            }

        val lobbyRedirector = LeastTrafficServerAggregateHandler(
            lobbyGroup().suffixWhenDev()
        )
        lobbyRedirector.subscribe()

        flavor {
            bind<ServerAggregateHandler>() to lobbyRedirector
        }

        Bukkit.dispatchCommand(
            Bukkit.getConsoleSender(),
            "spark profiler start --thread * --only-ticks-over 25"
        )
    }
}
