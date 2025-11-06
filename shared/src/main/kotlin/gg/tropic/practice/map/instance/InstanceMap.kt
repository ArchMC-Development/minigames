package gg.tropic.practice.map.instance

import gg.scala.commons.spatial.toPosition
import gg.tropic.practice.games.team.TeamIdentifier
import gg.tropic.practice.map.Map
import gg.tropic.practice.map.metadata.AbstractMapMetadata
import gg.tropic.practice.map.metadata.impl.MapLevelMetadata
import gg.tropic.practice.map.metadata.impl.MapSpawnMetadata
import gg.tropic.practice.map.metadata.impl.MapZoneMetadata
import gg.tropic.practice.map.utilities.MapMetadata
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack

/**
 * @author GrowlyX
 * @since 9/21/2023
 */
data class InstanceMap(
    val map: Map,
    val world: World,
    val compositeMeta: List<AbstractMapMetadata>
)
{
    val metadata = InstanceMapMetadata(
        mapMetadata = backingMetadata,
        metadata = compositeMeta
    )

    val name: String
        get() = map.name
    val backingMetadata: MapMetadata
        get() = map.metadata
    val displayName: String
        get() = map.displayName
    val displayIcon: ItemStack
        get() = map.displayIcon
    val associatedSlimeTemplate: String
        get() = map.associatedSlimeTemplate
    val associatedKitGroups: MutableSet<String>
        get() = map.associatedKitGroups
    val locked: Boolean
        get() = map.locked

    fun findSpawnLocations() = compositeMeta
        .filterIsInstance<MapSpawnMetadata>()

    fun findMapLevelRestrictions() = compositeMeta
        .filterIsInstance<MapLevelMetadata>()
        .firstOrNull()

    fun findMapLevelMinMax() = compositeMeta
        .filterIsInstance<MapLevelMetadata>()

    fun findZoneContainingEntity(entity: Entity) = compositeMeta
        .filterIsInstance<MapZoneMetadata>()
        .firstOrNull {
            it.bounds.contains(entity.location.toPosition())
        }

    fun findZoneContainingBlock(block: Block) = compositeMeta
        .filterIsInstance<MapZoneMetadata>()
        .firstOrNull {
            it.bounds.contains(block.location.toPosition())
        }

    fun findSpawnLocationMatchingTeam(team: TeamIdentifier) = compositeMeta
        .filterIsInstance<MapSpawnMetadata>()
        .firstOrNull {
            it.id == team.label.lowercase()
        }
        ?.position

    fun findSpawnLocationMatchingSpec() = compositeMeta
        .filterIsInstance<MapSpawnMetadata>()
        .firstOrNull {
            it.id == "spec"
        }
        ?.position
}
