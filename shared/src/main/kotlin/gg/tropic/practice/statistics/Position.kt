package gg.tropic.practice.statistics

import java.util.UUID

data class LeaderboardPosition(
    val uniqueId: UUID,
    val score: Long,
    val position: Long
)
