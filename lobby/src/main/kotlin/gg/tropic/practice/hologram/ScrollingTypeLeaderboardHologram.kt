package gg.tropic.practice.hologram

import gg.tropic.practice.kit.KitService
import gg.tropic.practice.leaderboards.CommonlyViewedLeaderboardType
import gg.tropic.practice.leaderboards.StatisticLeaderboard
import org.bukkit.Location

/**
 * @author GrowlyX
 * @since 12/29/2023
 */
class ScrollingTypeLeaderboardHologram(
    private val kit: String?,
    private val leaderboardTypes: List<CommonlyViewedLeaderboardType>,
    scrollTime: Int, location: Location
) : AbstractScrollingLeaderboard(scrollTime, location)
{
    override fun getNextReference(current: StatisticLeaderboard?) = StatisticLeaderboard(
        commonlyViewedLeaderboardType = current?.commonlyViewedLeaderboardType
            ?.let {
                leaderboardTypes.getOrNull(
                    leaderboardTypes.indexOf(it) + 1
                )
            }
            ?: leaderboardTypes.first(),
        kit = kit?.let { KitService.cached().kits[it] }
    )

    override fun getAbstractType() = ScrollingTypeLeaderboardHologram::class.java
}
