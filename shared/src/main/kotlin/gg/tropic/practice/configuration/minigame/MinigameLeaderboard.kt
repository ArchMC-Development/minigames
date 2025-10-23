package gg.tropic.practice.configuration.minigame

import gg.scala.commons.spatial.Position

/**
 * @author Subham
 * @since 6/24/25
 */
data class MinigameLeaderboard(
    var position: Position,
    var statisticID: String,
    var displayName: String
)
