package gg.tropic.practice.games.event

import gg.scala.commons.event.StatefulEvent
import gg.scala.commons.event.StatelessEvent
import gg.tropic.practice.games.GameImpl
import org.bukkit.entity.Player
import java.util.UUID

/**
 * @author GrowlyX
 * @since 8/24/2024
 */
data class AsyncPlayerPreJoinGameEvent(
    val game: GameImpl,
    val joiningPlayer: UUID
) : StatefulEvent()
