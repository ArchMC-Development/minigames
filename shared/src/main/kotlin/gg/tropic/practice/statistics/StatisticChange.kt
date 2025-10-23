package gg.tropic.practice.statistics

/**
 * @author Subham
 * @since 6/22/25
 */
data class StatisticChange(
    val id: String,
    val old: LeaderboardPosition,
    val new: LeaderboardPosition,
    val next: LeaderboardPosition?
)
{
    fun requiredScore() = next!!.score - new.score
}
