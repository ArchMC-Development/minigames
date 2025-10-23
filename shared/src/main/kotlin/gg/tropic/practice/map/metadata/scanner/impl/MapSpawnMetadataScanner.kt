package gg.tropic.practice.map.metadata.scanner.impl

import gg.scala.commons.spatial.toPosition
import gg.tropic.practice.map.metadata.impl.MapSpawnMetadata
import gg.tropic.practice.map.metadata.scanner.AbstractMapMetadataScanner
import gg.tropic.practice.map.metadata.sign.MapSignMetadataModel
import gg.tropic.practice.map.metadata.sign.manualMappings
import org.bukkit.World
import org.bukkit.material.Sign

/**
 * @author GrowlyX
 * @since 11/10/2022
 */
object MapSpawnMetadataScanner : AbstractMapMetadataScanner<MapSpawnMetadata>()
{
    override val type = "spawn"

    override fun scan(
        id: String, models: List<MapSignMetadataModel>, world: World
    ): MapSpawnMetadata
    {
        val model = models.first()
        val location = model.location.toLocation(world)
        if (location.block.state.data !is Sign)
        {
            return MapSpawnMetadata(id, location.toPosition())
        }

        val sign = model.location.toLocation(world).block.state.data as Sign
        location.yaw = manualMappings[sign.facing]!!

        location.z += 0.500F
        location.x += 0.500F

        return MapSpawnMetadata(id, location.toPosition())
    }
}
