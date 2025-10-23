package gg.tropic.practice.games.spectate

import java.util.*

/**
 * @author GrowlyX
 * @since 10/20/2023
 */
data class PlayerSpectateRequest(
    val player: UUID,
    val target: UUID,
    val bypassesSpectatorAllowanceChecks: Boolean = false
)
