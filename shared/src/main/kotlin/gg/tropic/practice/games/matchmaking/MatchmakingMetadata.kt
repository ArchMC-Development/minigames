package gg.tropic.practice.games.matchmaking

import gg.tropic.practice.region.Region

/**
 * @author GrowlyX
 * @since 8/24/2024
 */
data class MatchmakingMetadata(
    val region: Region,
    val bracket: String?
)
