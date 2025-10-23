package gg.tropic.practice.extensions

import gg.scala.commons.spatial.Position
import org.bukkit.Location

/**
 * @author Subham
 * @since 5/31/25
 */
fun Location.toPositionCentered() = Position(
    x = x + 0.5,
    y = y,
    z = z + 0.5,
    yaw = yaw,
    pitch = pitch
)
