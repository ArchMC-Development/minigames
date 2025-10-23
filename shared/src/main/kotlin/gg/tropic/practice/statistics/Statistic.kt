package gg.tropic.practice.statistics

import gg.tropic.practice.leaderboards.LeaderboardEntry
import gg.tropic.practice.profile.PracticeProfile
import io.lettuce.core.ScoredValue

/**
 * @author Subham
 * @since 6/18/25
 */
interface Statistic
{
    val id: StatisticID
    val defaultValue: Long
    val reversed: Boolean

    fun asyncLoadTopTenPlayers(): List<LeaderboardEntry>

    context(PracticeProfile) fun percentile(): Float
    context(PracticeProfile) fun scoreAndPosition(): ScoredValue<Long>

    context(PracticeProfile) fun update(newValue: Long)
    context(PracticeProfile) fun add(newValue: Long)
    context(PracticeProfile) fun subtract(newValue: Long)
}
