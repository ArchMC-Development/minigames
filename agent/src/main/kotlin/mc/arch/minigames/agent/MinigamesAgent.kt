package mc.arch.minigames.agent

import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.annotations.ServiceablePackage
import gg.scala.commons.annotations.container.ContainerEnable
import gg.scala.commons.core.plugin.*
import gg.tropic.practice.MinigamesAgentPlugin
import gg.tropic.practice.PracticeShared
import gg.tropic.practice.devProvider
import gg.tropic.practice.minigame.MiniGameSerializers

/**
 * @author Subham
 * @since 7/22/25
 */

@Plugin(
    name = "Minigames",
    version = "%remote%/%branch%/%id%",
    description = "Agent"
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
@ServiceablePackage("gg.tropic.practice")
class MinigamesAgent : ExtendedScalaPlugin(), MinigamesAgentPlugin
{
    init
    {
        PracticeShared
    }

    @ContainerEnable
    fun containerEnable()
    {
        devProvider = { false }
        MiniGameSerializers.configure()
    }
}
