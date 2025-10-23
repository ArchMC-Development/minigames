package gg.tropic.practice.map.metadata.impl

import gg.tropic.practice.map.metadata.AbstractMapMetadata
import gg.scala.commons.spatial.Orientation
import gg.scala.commons.spatial.Position

/**
 * @author GrowlyX
 * @since 11/10/2022
 */
data class MapBedMetadata(
    val position: Position,
    val orientation: Orientation,
    override val id: String
) : AbstractMapMetadata()
{
    override fun report() = "Bed | $id | At $position | Facing ${orientation.name}"
    override fun getAbstractType() = MapBedMetadata::class.java
}
