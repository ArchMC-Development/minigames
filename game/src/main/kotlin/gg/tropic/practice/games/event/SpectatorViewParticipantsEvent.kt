package gg.tropic.practice.games.event

import gg.scala.commons.event.StatelessEvent
import gg.tropic.practice.games.GameImpl
import gg.tropic.practice.minigame.AbstractMiniGameGameImpl
import gg.tropic.practice.minigame.spectators.PlayerParticipant
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 8/24/2024
 */
data class SpectatorViewParticipantsEvent(
    val game: GameImpl,
    val viewer: Player,
    val players: List<PlayerParticipant>
) : StatelessEvent()
