package gg.tropic.practice.games.spectate

import gg.tropic.practice.games.GameReference
import java.util.UUID

/**
 * @author GrowlyX
 * @since 10/20/2023
 */
data class SpectateRequest(
    val server: String,
    val gameId: UUID,
    val player: UUID,
    val target: UUID,
    val bypassesSpectatorAllowanceChecks: Boolean = false
)
