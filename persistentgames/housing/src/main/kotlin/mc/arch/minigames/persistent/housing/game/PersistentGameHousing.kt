package mc.arch.minigames.persistent.housing.game

import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.annotations.container.ContainerEnable
import gg.scala.commons.core.plugin.Plugin
import gg.scala.commons.core.plugin.PluginAuthor
import gg.scala.commons.core.plugin.PluginAuthorComposite
import gg.scala.commons.core.plugin.PluginDependency
import gg.scala.commons.core.plugin.PluginDependencyComposite
import gg.scala.commons.core.plugin.PluginWebsite

@Plugin(
    name = "Housing",
    version = "%remote%/%branch%/%id%"
)
@PluginAuthorComposite(
    PluginAuthor("GrowlyX"),
    PluginAuthor("98ping"),
)
@PluginWebsite("https://arch.mc")
@PluginDependencyComposite(
    PluginDependency("scala-commons"),
    PluginDependency("Lemon"),
    PluginDependency("Minigames"),
)
class PersistentGameHousing : ExtendedScalaPlugin()
{
    companion object
    {
        lateinit var instance: PersistentGameHousing
    }


    @ContainerEnable
    fun containerEnable()
    {
        instance = this
    }
}
