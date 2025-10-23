package gg.tropic.practice.games.elo

/**
 * @author GrowlyX
 * @since 8/15/2024
 */
data class ELOUpdates(
    val winner: Pair<Int, Int>,
    val loser: Pair<Int, Int>,
)
