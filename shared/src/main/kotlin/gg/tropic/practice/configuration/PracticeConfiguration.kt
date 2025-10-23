package gg.tropic.practice.configuration

import gg.scala.commons.agnostic.sync.ServerSync
import gg.scala.commons.graduation.Progressive
import gg.scala.commons.spatial.Position
import gg.tropic.practice.configuration.PracticeConfigurationService.typeProvider
import gg.tropic.practice.editor.EditableUI
import gg.tropic.practice.parkour.ParkourConfiguration
import net.evilblock.cubed.util.CC

/**
 * @author GrowlyX
 * @since 10/13/2023
 */
data class PracticeConfiguration(
    var rankedQueueEnabled: Boolean = true,
    @Deprecated("Use current()")
    internal var spawnLocation: Position = Position(
        0.0, 100.0, 0.0, 180.0F, 0.0F
    ),
    @Deprecated("Use current()")
    internal val loginMOTD: MutableList<String> = mutableListOf(
        "",
        "${CC.B_PRI}Welcome to Tropic Practice",
        "${CC.GRAY}We are currently in BETA! Report bugs in our Discord.",
        ""
    ),
    @Deprecated("Use current()")
    internal var parkourConfiguration: ParkourConfiguration? = null,
    var externalPlayerCountBaseUrl: String = "http://external-playercount-app.default.svc.cluster.local:8080",
    var minigameConfigurations: MutableMap<String, MinigameLobbyConfiguration> = mutableMapOf(),
    var editableUIs: MutableMap<String, EditableUI> = mutableMapOf(),
    override var matured: Set<String>? = setOf(),
) : Progressive
{
    fun local(): MinigameLobbyConfiguration
    {
        if (ServerSync.local.groups.contains("hub"))
        {
            val config = minigameConfigurations["main"]
            if (config == null)
            {
                minigameConfigurations["main"] = MinigameLobbyConfiguration()
                return minigameConfigurations["main"]!!
            }

            return config
        }

        return typeProvider
            ?.let { minigameConfigurations[it.provide().internalId] }
            ?: minigameConfigurations["duels"]
            ?: run {
                minigameConfigurations["duels"] = MinigameLobbyConfiguration()
                minigameConfigurations["duels"]!!
            }
    }
}
