package gg.tropic.practice.minigame

import gg.tropic.practice.profile.PracticeProfile
import net.evilblock.cubed.menu.Button
import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 6/15/25
 */
interface MinigameLobbyCustomizer
{
    val id: String

    fun mainMenuProvider(player: Player)
    fun playProvider(player: Player)

    fun leaderboardsProvider(player: Player): Map<Int, Button>
    fun statisticsMenuProvider(profile: PracticeProfile): Map<Int, Button>

    fun holographicStatsProvider(player: Player): List<String>

    fun scoreboard(): MinigameLobbyScoreboardProvider
}
