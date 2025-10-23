package gg.tropic.practice.statistics

/**
 * @author Subham
 * @since 6/18/25
 */
enum class TrackedKitStatistic(val timeSensitive: List<StatisticLifetime> = emptyList())
{
    Plays, Losses, Kills, Deaths,
    WinStreak(listOf(StatisticLifetime.Daily)),
    WinStreakHighest,
    ELO,
    Wins(listOf(StatisticLifetime.Daily))
}
