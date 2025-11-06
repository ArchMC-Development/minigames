package gg.tropic.practice.map.instance

import gg.tropic.practice.map.metadata.AbstractMapMetadata
import gg.tropic.practice.map.utilities.MapMetadata
import org.bukkit.World

/**
 * @author Subham
 * @since 11/5/25
 */
class InstanceMapMetadata(
    val mapMetadata: MapMetadata,
    val metadata: List<AbstractMapMetadata>
)
{
    fun composite(world: World) = metadata
    fun synthetics(world: World) = mapMetadata.synthetics(world)

    fun clearSignLocations(world: World)
    {
        mapMetadata.clearSignLocations(world)
    }
}
