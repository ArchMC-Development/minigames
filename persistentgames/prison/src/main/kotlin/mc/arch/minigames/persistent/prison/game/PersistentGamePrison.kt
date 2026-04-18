package mc.arch.minigames.persistent.prison.game

import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.annotations.ServiceablePackage
import gg.scala.commons.annotations.container.ContainerEnable
import gg.scala.commons.core.plugin.Plugin
import gg.scala.commons.core.plugin.PluginAuthor
import gg.scala.commons.core.plugin.PluginAuthorComposite
import gg.scala.commons.core.plugin.PluginDependency
import gg.scala.commons.core.plugin.PluginDependencyComposite
import gg.scala.commons.core.plugin.PluginWebsite
import mc.arch.minigames.persistent.prison.shared.PrisonPluginHolder

@Plugin(
    name = "Prison",
    version = "%remote%/%branch%/%id%"
)
@PluginAuthorComposite(
    PluginAuthor("ArchMC"),
)
@PluginWebsite("https://arch.mc")
@PluginDependencyComposite(
    PluginDependency("scala-commons"),
    PluginDependency("Lemon"),
    PluginDependency("Minigames"),
)
@ServiceablePackage("mc.arch.minigames.persistent.prison.shared")
class PersistentGamePrison : ExtendedScalaPlugin()
{
    companion object
    {
        lateinit var instance: PersistentGamePrison
    }

    @ContainerEnable
    fun containerEnable()
    {
        instance = this
        PrisonPluginHolder.register(this)
    }
}
