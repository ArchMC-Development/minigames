package mc.arch.minigames.persistent.housing.game.spatial

import gg.scala.commons.spatial.toPosition
import gg.tropic.practice.map.metadata.impl.MapZoneMetadata
import mc.arch.minigames.persistent.housing.game.resources.getPlayerHouseFromInstance
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
    fun configure(bounds: Int, mapZoneMetadata: MapZoneMetadata, world: World)
    {
        val zero = Location(world, 0.0, 100.0, 0.0)
        val regionAsCuboid = Cuboid(
            zero.clone().subtract(bounds.toDouble(), 100.0, bounds.toDouble()),
            zero.clone().add(bounds.toDouble(), 156.0, bounds.toDouble())
        )

        // is this jank? Yes
        // is this how we forcibly load shit? Also yes
        Tasks.sync {
            regionAsCuboid.getChunks().forEach { chunk ->
                val center = Location(
                    chunk.world,
                    (chunk.x * 16) + 7.5,
                    102.0,
                    (chunk.z * 16) + 7.5
                )

                val originalType = center.world
                    .getBlockAt(center)
                    .type

                center.world
                    .getBlockAt(center)
                    .setType(
                        Material.BEDROCK, true
                    )

                center.world
                    .getBlockAt(center)
                    .setType(
                        originalType, true
                    )

                println("Placing block to update chunk ${chunk.x},${chunk.z}")
            }
        }

        Events.subscribe(BlockBreakEvent::class.java)
            .handler { event ->
                val player = event.player
                val location = event.block.location
                val region = mapZoneMetadata.bounds
                val house = player.getPlayerHouseFromInstance()

                if (!region.contains(location.toPosition()))
                {
                    if (house?.allowsMutatingOutsideRegion != true)
                    {
                        event.isCancelled = true
                    }
                }
            }

        Events.subscribe(BlockPlaceEvent::class.java)
            .handler { event ->
                val player = event.player
                val location = event.block.location
                val region = mapZoneMetadata.bounds
                val house = player.getPlayerHouseFromInstance()

                if (!region.contains(location.toPosition()))
                {
                    if (house?.allowsMutatingOutsideRegion != true)
                    {
                        event.isCancelled = true
                    }
                }
            }
    }
}