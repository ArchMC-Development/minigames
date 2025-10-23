package gg.tropic.practice.map

import com.cryptomorin.xseries.XMaterial
import gg.tropic.practice.games.team.TeamIdentifier
import gg.scala.commons.spatial.Bounds
import gg.scala.commons.spatial.toPosition
import gg.tropic.practice.map.metadata.impl.MapLevelMetadata
import gg.tropic.practice.map.metadata.impl.MapSpawnMetadata
import gg.tropic.practice.map.metadata.impl.MapZoneMetadata
import gg.tropic.practice.map.utilities.MapMetadata
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack

/**
 * Defines a map. Maps and available replications are decoupled, so
 * we don't have to worry about constantly synchronizing the [MapService]
 * when new replications are available or if a replication is deleted.
 *
 * @author GrowlyX
 * @since 9/21/2023
 */
data class Map(
    var name: String,
    var metadata: MapMetadata,
    var displayName: String,
    var displayIcon: ItemStack = ItemBuilder
        .of(XMaterial.MAP)
        .build(),
    val associatedSlimeTemplate: String,
    val associatedKitGroups: MutableSet<String> =
        mutableSetOf("__default__")
)
{
    var locked = false

    fun findSpawnLocations() = metadata
        .metadata
        .filterIsInstance<MapSpawnMetadata>()

    fun findMapLevelRestrictions() = metadata
        .metadata
        .filterIsInstance<MapLevelMetadata>()
        .firstOrNull()

    fun findMapLevelMinMax() = metadata
        .metadata
        .filterIsInstance<MapLevelMetadata>()

    fun findZoneContainingEntity(entity: Entity) = metadata
        .metadata
        .filterIsInstance<MapZoneMetadata>()
        .firstOrNull {
            it.bounds.contains(entity.location.toPosition())
        }

    fun findZoneContainingBlock(block: Block) = metadata
        .metadata
        .filterIsInstance<MapZoneMetadata>()
        .firstOrNull {
            it.bounds.contains(block.location.toPosition())
        }

    fun findSpawnLocationMatchingTeam(team: TeamIdentifier) = metadata
        .metadata
        .filterIsInstance<MapSpawnMetadata>()
        .firstOrNull {
            it.id == team.label.lowercase()
        }
        ?.position

    fun findSpawnLocationMatchingSpec() = metadata
        .metadata
        .filterIsInstance<MapSpawnMetadata>()
        .firstOrNull {
            it.id == "spec"
        }
        ?.position
}
