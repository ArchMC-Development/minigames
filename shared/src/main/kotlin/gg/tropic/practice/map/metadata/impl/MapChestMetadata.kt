package gg.tropic.practice.map.metadata.impl

import gg.tropic.practice.games.team.TeamIdentifier
import gg.tropic.practice.map.metadata.AbstractMapMetadata
import gg.scala.commons.spatial.Position

/**
 * @author GrowlyX
 * @since 11/10/2022
 */
data class MapChestMetadata(
    override val id: String,
    var locations: Map<String, List<Position>>
) : AbstractMapMetadata()
{
    fun getGlobal() = locations["global"] ?: listOf()
    fun getByTeam(teamIdentifier: TeamIdentifier) = locations[teamIdentifier.label.lowercase()] ?: listOf()

    override fun report() = "Chests | $id | ${locations.size} group (${locations.entries.joinToString(", ") { "${it.key} -> ${it.value.size}"}})"
    override fun getAbstractType() = MapChestMetadata::class.java
}
