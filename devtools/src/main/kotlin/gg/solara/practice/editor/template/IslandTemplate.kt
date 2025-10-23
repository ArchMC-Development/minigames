package gg.solara.practice.editor.template

import gg.scala.commons.spatial.Position

/**
 * @author Subham
 * @since 5/28/25
 */
data class IslandTemplate(
    val basePosition: Position,
    val chestDiffs: Position,
    val enderChestDiffs: Position,
    val shopDiffs: Position,
    val upgradeDiffs: Position,
    val bedBlockDiffs: List<Position>
)
