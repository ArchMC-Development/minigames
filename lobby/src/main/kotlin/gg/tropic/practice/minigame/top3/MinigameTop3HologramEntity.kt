package gg.tropic.practice.minigame.top3

import gg.tropic.practice.configuration.minigame.MinigameTopPlayerNPCSet
import gg.tropic.practice.profile.PracticeProfileService
import gg.tropic.practice.services.LeaderboardManagerService
import net.evilblock.cubed.entity.EntityHandler
import net.evilblock.cubed.entity.hologram.personalized.PersonalizedHologramEntity
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.math.Numbers
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 6/30/25
 */
data class MinigameTop3HologramEntity(
    private val leaderboard: MinigameTopPlayerNPCSet
) : PersonalizedHologramEntity(
    leaderboard.hologram
        .toLocation(Bukkit.getWorlds().first())
        .clone()
        .subtract(0.0, 0.7, 0.0)
)
{
    init
    {
        persistent = false
    }

    fun configure()
    {
        initializeData()
        EntityHandler.trackEntity(this)
    }

    override fun getNewLines(player: Player): List<String>
    {
        val lines = mutableListOf<String>()
        lines += "${CC.PRI}${leaderboard.displayName}"
        lines += ""

        val value = PracticeProfileService.find(player)
            ?.getStatisticValue(
                leaderboard.statisticID
            )

        val leaderboards = LeaderboardManagerService
            .getCachedFormattedLeaderboards(leaderboard.statisticID)
            .toMutableList()

        leaderboards.removeFirstOrNull()
        leaderboards.removeFirstOrNull()
        leaderboards.removeFirstOrNull()

        lines += if (leaderboards.isEmpty())
        {
            listOf("${CC.GRAY}Loading...")
        } else
        {
            leaderboards
        }

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
        }

        return lines
    }

    override fun getUpdateInterval() = 10_000L
}
