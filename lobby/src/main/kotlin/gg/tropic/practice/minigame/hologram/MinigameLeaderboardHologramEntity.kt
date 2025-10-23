package gg.tropic.practice.minigame.hologram

import gg.tropic.practice.configuration.minigame.MinigameLeaderboard
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
data class MinigameLeaderboardHologramEntity(
    private val leaderboard: MinigameLeaderboard
) : PersonalizedHologramEntity(
    leaderboard.position.toLocation(Bukkit.getWorlds().first())
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

        val value = PracticeProfileService.find(player)
            ?.getStatisticValue(
                leaderboard.statisticID
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
            .getCachedFormattedLeaderboards(leaderboard.statisticID)

        return lines
    }

    override fun getUpdateInterval() = 10_000L
}
