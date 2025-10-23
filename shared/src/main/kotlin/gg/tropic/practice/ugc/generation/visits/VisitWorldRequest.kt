package gg.tropic.practice.ugc.generation.visits

import gg.tropic.practice.ugc.WorldInstanceProviderType
import java.util.UUID

/**
 * @author Subham
 * @since 7/18/25
 */
data class VisitWorldRequest(
    val ownerPlayerId: UUID,
    val visitingPlayers: Set<UUID>,
    val worldGlobalId: UUID,
    val configuration: VisitWorldRequestConfiguration,
    val providerType: WorldInstanceProviderType,
)
