package mc.arch.minigames.persistent.housing.game.spatial

import gg.scala.commons.spatial.toPosition
import gg.tropic.practice.map.metadata.impl.MapZoneMetadata
import me.lucko.helper.Events
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.bukkit.cuboid.Cuboid
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent

object SpatialZoneService
{
    fun configure(mapZoneMetadata: MapZoneMetadata, world: World)
    {
        val regionAsCuboid = Cuboid(
            mapZoneMetadata.lower.toLocation(world),
            mapZoneMetadata.top.toLocation(world)
        )

        // is this jank? Yes
        // is this how we forcibly load shit? Also yes
        Tasks.sync {
            regionAsCuboid.getChunks().forEach { chunk ->
                val center = Location(
                    chunk.world,
                    (chunk.x * 16) + 7.5,
                    100.0,
                    (chunk.z * 16) + 7.5
                )

                center.world
                    .getBlockAt(center)
                    .setType(
                        Material.AIR, true
                    )

                println("Placing block to update chunk ${chunk.x},${chunk.z}")
            }
        }

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