package gg.tropic.practice.hologram

import gg.tropic.practice.kit.KitService
import gg.tropic.practice.leaderboards.CommonlyViewedLeaderboardType
import gg.tropic.practice.leaderboards.StatisticLeaderboard
import org.bukkit.Location

/**
 * @author GrowlyX
 * @since 6/30/2023
 */
class ScrollingKitLeaderboardHologram(
    private val leaderboardType: CommonlyViewedLeaderboardType,
    private val kits: List<String>,
    scrollTime: Int, location: Location
) : AbstractScrollingLeaderboard(scrollTime, location)
{
    override fun getNextReference(current: StatisticLeaderboard?) = StatisticLeaderboard(
        commonlyViewedLeaderboardType = leaderboardType,
        kit = KitService.cached().kits[
            current?.kit?.id
                ?.let {
                    kits.getOrNull(
                        kits.indexOf(it) + 1
                    )
                }
                ?: kits.first()
        ]!!
    )

    override fun getAbstractType() = ScrollingKitLeaderboardHologram::class.java
}
