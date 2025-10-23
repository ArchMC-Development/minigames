package gg.tropic.practice.map.metadata.impl

import gg.tropic.practice.map.metadata.AbstractMapMetadata
import gg.scala.commons.spatial.Bounds
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
    fun fromCenter(center: Position) = Bounds(
        Position(center.x.toInt() - radius(), center.y.toInt() - radius(), center.z.toInt() - radius()),
        Position(center.x.toInt() + radius(), center.y.toInt() + radius(), center.z.toInt() + radius())
    )

    override fun report() = "Spawn Protection Expansion | ${id}x${id} square"
    override fun getAbstractType() = MapSpawnProtExpandMetadata::class.java
}
