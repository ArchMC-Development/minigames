package gg.tropic.practice.minigame.rejoin

import java.util.UUID

/**
 * @author Subham
 * @since 6/16/25
 */
data class RejoinToken(
    val server: String,
    val expectation: UUID,
    val expiration: Long,
    val gameDescription: String
)
