package mc.arch.lobby.main

import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.annotations.ServiceablePackage
import gg.scala.commons.annotations.container.ContainerEnable
import gg.scala.commons.core.plugin.*
import org.bukkit.Bukkit

/**
 * @author Subham
 * @since 6/27/25
 */
@Plugin(
    name = "Lobby",
    version = "%remote%/%branch%/%id%"
)
@PluginAuthor("ArchMC")
@PluginWebsite("https://arch.mc")
@PluginDependencyComposite(
    PluginDependency("scala-commons"),
    PluginDependency("Lemon"),
    PluginDependency("Minigames"),
)
@ServiceablePackage("mc.arch.pubapi.akers")
class MainLobby : ExtendedScalaPlugin()
