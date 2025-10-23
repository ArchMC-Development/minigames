package gg.tropic.practice.ugc.generation.addplayer

import java.util.UUID

/**
 * @author Subham
 * @since 7/24/25
 */
data class AddPlayerRequest(
    val globalWorldId: UUID,
    val server: String,
    val visitingPlayers: Set<UUID>
)
