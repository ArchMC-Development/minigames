package mc.arch.minigames.hungergames.lobby

import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.core.plugin.*

/**
 * @author ArchMC
 */
@Plugin(
    name = "HungerGames",
    version = "%remote%/%branch%/%id%"
)
@PluginAuthor("ArchMC")
@PluginWebsite("https://arch.mc")
@PluginDependencyComposite(
    PluginDependency("scala-commons"),
    PluginDependency("Lemon"),
    PluginDependency("Minigames"),
)
class HungerGamesLobby : ExtendedScalaPlugin()
