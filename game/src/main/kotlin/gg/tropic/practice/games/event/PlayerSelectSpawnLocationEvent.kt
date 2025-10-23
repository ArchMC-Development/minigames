package gg.tropic.practice.games.event

import gg.scala.commons.event.StatelessEvent
import gg.tropic.practice.games.GameImpl
import org.bukkit.Location
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 8/24/2024
 */
data class PlayerSelectSpawnLocationEvent(
    val game: GameImpl,
    val player: Player,
    var location: Location,
    val spectator: Boolean
) : StatelessEvent()
