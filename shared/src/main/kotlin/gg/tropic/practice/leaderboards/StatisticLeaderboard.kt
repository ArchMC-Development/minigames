package gg.tropic.practice.leaderboards

import gg.tropic.practice.kit.Kit

/**
 * @author Subham
 * @since 6/23/25
 */
data class StatisticLeaderboard(
    val commonlyViewedLeaderboardType: CommonlyViewedLeaderboardType,
    val kit: Kit?
)
