package gg.tropic.practice.map.metadata.impl

import gg.tropic.practice.currency.ShopCurrency
import gg.tropic.practice.games.team.TeamIdentifier
import gg.tropic.practice.map.metadata.AbstractMapMetadata
import gg.scala.commons.spatial.Position

/**
 * @author GrowlyX
 * @since 11/10/2022
 */
data class MapGenMetadata(
    override val id: String,
    var locations: Map<String, List<Position>>
) : AbstractMapMetadata()
{
    fun getByCurrency(currency: ShopCurrency) = locations[currency.name.lowercase()] ?: listOf()
    fun getByTeam(teamIdentifier: TeamIdentifier) = locations[teamIdentifier.label.lowercase()] ?: listOf()

    override fun report() = "Gen | $id | ${locations.size} group (${locations.entries.joinToString(", ") { "${it.key} -> ${it.value.size}"}})"
    override fun getAbstractType() = MapGenMetadata::class.java
}
