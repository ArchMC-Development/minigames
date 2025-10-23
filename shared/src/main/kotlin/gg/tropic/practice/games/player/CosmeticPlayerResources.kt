package gg.tropic.practice.games.player

import java.util.UUID

/**
 * @author Subham
 * @since 7/22/25
 */
data class CosmeticPlayerResources(
    val playerId: UUID,
    val username: String,
    val displayName: String,
    val disguised: Boolean
)
