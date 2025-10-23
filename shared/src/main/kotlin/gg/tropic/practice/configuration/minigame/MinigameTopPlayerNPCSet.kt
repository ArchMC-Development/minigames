package gg.tropic.practice.configuration.minigame

import gg.scala.commons.spatial.Position

/**
 * @author Subham
 * @since 6/24/25
 */
data class MinigameTopPlayerNPCSet(
    var statisticID: String = "replace",
    var statisticDisplayName: String = "kill",
    var displayName: String = "Template Set",
    var first: Position = Position(0.0, 0.0, 0.0),
    var second: Position = Position(0.0, 0.0, 0.0),
    var third: Position = Position(0.0, 0.0, 0.0),
    var hologram: Position = Position(0.0, 0.0, 0.0)
)
