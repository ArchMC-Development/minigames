package gg.tropic.practice.ugc

import java.util.UUID

/**
 * @author Subham
 * @since 7/18/25
 */
data class HostedWorldInstanceReference(
    val globalId: UUID,
    val nameId: String,
    val ownerPlayerId: UUID,
    val type: WorldInstanceProviderType,
    val state: HostedWorldState,
    val onlinePlayers: Set<UUID>,
    val server: String,
    val loadTime: Long
)
