package gg.tropic.practice.map.metadata.impl

import gg.tropic.practice.map.metadata.AbstractMapMetadata
import gg.scala.commons.spatial.Position

/**
 * @author GrowlyX
 * @since 11/10/2022
 */
data class MapSpawnMetadata(
    override val id: String,
    var position: Position
) : AbstractMapMetadata()
{
    override fun report() = "Spawn | $id | $position"
    override fun getAbstractType() = MapSpawnMetadata::class.java
}
