package gg.tropic.practice

import gg.scala.basics.plugin.settings.SettingMenu
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.agnostic.sync.ServerSync
import gg.scala.commons.annotations.ServiceablePackage
import gg.scala.commons.annotations.container.ContainerEnable
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.core.plugin.*
import gg.tropic.practice.minigame.MiniGameSerializers

/**
 * @author GrowlyX
 * @since 8/5/2022
 */
@Plugin(
    name = "Minigames",
    version = "%remote%/%branch%/%id%",
    description = "Lobby"
)
@PluginAuthor("ArchMC")
@PluginWebsite("https://arch.mc")
@PluginDependencyComposite(
    PluginDependency("scala-commons"),
    PluginDependency("Lemon"),
    PluginDependency("ScBasics"),
    PluginDependency("Parties"),
    PluginDependency("CoreGameExtensions"),
    PluginDependency("ScStaff", soft = true),
    PluginDependency("ScQueue", soft = true),
    PluginDependency("PlaceholderAPI", soft = true),
    PluginDependency("SkinsRestorer", soft = true),
    PluginDependency("Friends", soft = true),
)
@ServiceablePackage("mc.arch.minigames.microgames.bridging")
class PracticeLobby : ExtendedScalaPlugin()
{
    init
    {
        PracticeShared
    }

    @ContainerEnable
    fun containerEnable()
    {
        devProvider = {
            "miplobbydev" in ServerSync.getLocalGameServer().groups
        }

        MiniGameSerializers.configure()
        SettingMenu.defaultCategory = "Minigames"
    }

    fun unregisterCommands(
        vararg commands: ScalaCommand
    )
    {
        commands.forEach {
            commandManager.unregisterCommand(it)
        }
    }
}
