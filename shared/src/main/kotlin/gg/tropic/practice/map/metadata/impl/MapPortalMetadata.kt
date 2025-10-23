package gg.tropic.practice.map.metadata.impl

import gg.tropic.practice.map.metadata.AbstractMapMetadata
import gg.scala.commons.spatial.Bounds
import gg.scala.commons.spatial.Position

/**
 * @author GrowlyX
 * @since 11/10/2022
 */
data class MapPortalMetadata(
    override val id: String,
    var lower: Position,
    var top: Position,
    val bounds: Bounds = Bounds(lower, top)
) : AbstractMapMetadata()
{
    override fun report() = "Portal | $id | $lower -> $top"
    override fun getAbstractType() = MapPortalMetadata::class.java
}
