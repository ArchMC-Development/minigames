package gg.tropic.practice.hologram

import gg.tropic.practice.leaderboards.StatisticLeaderboard
import gg.tropic.practice.profile.PracticeProfileService
import gg.tropic.practice.services.LeaderboardManagerService
import net.evilblock.cubed.entity.EntityHandler
import net.evilblock.cubed.entity.hologram.personalized.PersonalizedHologramEntity
import net.evilblock.cubed.serializers.impl.AbstractTypeSerializable
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.math.Numbers
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.Location
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 12/30/2023
 */
abstract class AbstractScrollingLeaderboard(
    val scrollTime: Int,
    location: Location
) : AbstractTypeSerializable, PersonalizedHologramEntity(location)
{
    @Transient
    var secondsUntilRefresh: Int? = null
        get()
        {
            if (field == null)
            {
                field = scrollTime
            }

            return field!!
        }

    fun configure()
    {
        initializeData()
        EntityHandler.trackEntity(this)
    }

    internal var currentLeaderboardID: StatisticLeaderboard? = null
    abstract fun getNextReference(current: StatisticLeaderboard?): StatisticLeaderboard

    override fun getUpdateInterval() = 1000L
    override fun getNewLines(player: Player): List<String>
    {
        val lines = mutableListOf<String>()
        val currentRef = currentLeaderboardID
            ?: return listOf(
                "${CC.GRAY}Loading..."
            )

        lines += "${CC.PRI}${
            currentRef.kit?.displayName ?: "Global"
        }${CC.PRI} ${
            currentRef.commonlyViewedLeaderboardType.displayName
        }"

        val statisticID = currentRef
            .commonlyViewedLeaderboardType
            .toStatisticID(currentRef.kit)

        val value = PracticeProfileService.find(player)
            ?.getCachedStatisticValueWithDeferredEnqueue(
                statisticID
            )

        if (value != null)
        {
            lines += ""
            lines += "${CC.SEC}You: ${CC.WHITE}${
                Numbers.format(value.score.toLong())
            } ${
                if (value.value != -1L) "${CC.GRAY}(#${
                    Numbers.format(value.value + 1)
                })" else ""
            }"
            lines += ""
        }

        lines += LeaderboardManagerService
            .getCachedFormattedLeaderboards(statisticID.toId())

        lines += ""
        lines += "${CC.GRAY}Switches in ${CC.WHITE}${
            TimeUtil.formatIntoMMSS(secondsUntilRefresh ?: 10)
        }${CC.GRAY}..."

        return lines
    }
}
