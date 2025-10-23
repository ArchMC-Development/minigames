package gg.tropic.practice.configuration.minigame

import gg.scala.commons.spatial.Bounds
import gg.scala.commons.spatial.Position

/**
 * @author Subham
 * @since 7/15/25
 */
data class MinigameBezierTeleporter(
    var start: Bounds = Bounds(
        Position(0.0, 0.0, 0.0),
        Position(0.0, 0.0, 0.0)
    ),
    var end: Position = Position(0.0, 0.0, 0.0),
    var duration: Int = 30,
    var height: Int = 7,
    var preset: MotionPreset = MotionPreset.BALANCED,
)
