package gg.tropic.practice.map.metadata.impl

import gg.tropic.practice.map.metadata.AbstractMapMetadata
import gg.scala.commons.spatial.Bounds
import gg.scala.commons.spatial.Box
import gg.scala.commons.spatial.Position
import kotlin.math.ceil
import kotlin.math.floor

/**
 * @author GrowlyX
 * @since 11/10/2022
 */
data class MapSpawnProtExpandMetadata(
    override val id: String,
) : AbstractMapMetadata()
{
    fun radius() = id.toInt().toDouble()
    fun fromCenter(center: Position) = Box(
        xSize = radius() * 2,
        zSize = radius() * 2,
        ySize = radius() * 2,
        center = center
    )

    override fun report() = "Spawn Protection Expansion | ${id}x${id} square"
    override fun getAbstractType() = MapSpawnProtExpandMetadata::class.java
}
