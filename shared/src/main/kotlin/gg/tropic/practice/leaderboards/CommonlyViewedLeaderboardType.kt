package gg.tropic.practice.leaderboards

import gg.tropic.practice.kit.Kit
import gg.tropic.practice.statistics.StatisticID
import gg.tropic.practice.statistics.TrackedKitStatistic
import gg.tropic.practice.statistics.statisticIdFrom

/**
 * @author GrowlyX
 * @since 9/25/2023
 */
enum class CommonlyViewedLeaderboardType(
    val displayName: String,
    val toStatisticID: (Kit?) -> StatisticID,
    val enforceRanked: Boolean = false
)
{
    ELO(
        displayName = "Ranked ELO",
        enforceRanked = true,
        toStatisticID = {
            statisticIdFrom(TrackedKitStatistic.ELO) {
                kit(it)
                ranked()
            }
        }
    ),
    CasualWins(
        displayName = "Casual Wins",
        toStatisticID = {
            statisticIdFrom(TrackedKitStatistic.Wins) {
                kit(it)
                casual()
            }
        }
    ),
    CasualWinsDaily(
        displayName = "Casual Wins (Daily)",
        toStatisticID = {
            statisticIdFrom(TrackedKitStatistic.Wins) {
                kit(it)
                daily()
                casual()
            }
        }
    ),
    RankedWins(
        displayName = "Ranked Wins",
        toStatisticID = {
            statisticIdFrom(TrackedKitStatistic.Wins) {
                kit(it)
                ranked()
            }
        }
    ),
    CasualWinStreak(
        displayName = "Casual Win Streak (Daily)",
        toStatisticID = {
            statisticIdFrom(TrackedKitStatistic.WinStreak) {
                kit(it)
                casual()
                daily()
            }
        }
    ),
    RankedWinStreak(
        displayName = "Ranked Win Streak (Daily)",
        enforceRanked = true,
        toStatisticID = {
            statisticIdFrom(TrackedKitStatistic.WinStreak) {
                kit(it)
                ranked()
                daily()
            }
        }
    );

    fun previous(): CommonlyViewedLeaderboardType
    {
        return CommonlyViewedLeaderboardType.entries
            .getOrNull(ordinal - 1)
            ?: CommonlyViewedLeaderboardType.entries.last()
    }

    fun next(): CommonlyViewedLeaderboardType
    {
        return CommonlyViewedLeaderboardType.entries
            .getOrNull(ordinal + 1)
            ?: CommonlyViewedLeaderboardType.entries.first()
    }
}
