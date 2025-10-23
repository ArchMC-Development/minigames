package gg.tropic.practice.configuration.minigame.levitationportal

import gg.scala.commons.spatial.Bounds

/**
 * @author Subham
 * @since 10/23/25
 */
data class LevitationPortalSpec(
    val id: String,
    val bounds: Bounds,
    val height: Double = 1.777,
    val limit: Double = 0.10,
    val restrictBelowAir: Boolean = true
)
