package gg.tropic.practice.minigame

import gg.tropic.practice.profile.PracticeProfile
import net.evilblock.cubed.menu.Button
import org.bukkit.entity.Player

interface MinigameCompetitiveCustomizer
{
    fun leaderboardsProvider(player: Player): Map<Int, Button>
    fun statisticsMenuProvider(profile: PracticeProfile): Map<Int, Button>

    fun holographicStatsProvider(player: Player): List<String>
}