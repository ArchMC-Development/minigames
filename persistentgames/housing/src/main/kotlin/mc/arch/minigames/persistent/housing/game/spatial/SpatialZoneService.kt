package mc.arch.minigames.persistent.housing.game.spatial

import gg.scala.commons.spatial.toPosition
import gg.tropic.practice.map.metadata.impl.MapZoneMetadata
import me.lucko.helper.Events
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent

object SpatialZoneService
{
    fun configure(mapZoneMetadata: MapZoneMetadata)
    {
        Events.subscribe(BlockBreakEvent::class.java)
            .handler { event ->
                val location = event.block.location
                val region = mapZoneMetadata.bounds

                if (!region.contains(location.toPosition()))
                {
                    event.isCancelled = true
                }
            }

        Events.subscribe(BlockPlaceEvent::class.java)
            .handler { event ->
                val location = event.block.location
                val region = mapZoneMetadata.bounds

                if (!region.contains(location.toPosition()))
                {
                    event.isCancelled = true
                }
            }
    }
}