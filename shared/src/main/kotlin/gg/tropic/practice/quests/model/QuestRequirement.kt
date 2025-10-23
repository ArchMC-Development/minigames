package gg.tropic.practice.quests.model

import gg.tropic.practice.statistics.StatisticID
import gg.tropic.practice.statistics.StatisticService
import gg.tropic.practice.statistics.statisticRead
import java.util.UUID

/**
 * @author Subham
 * @since 7/8/25
 */
data class QuestRequirement(
    var statisticID: String = "custom:bedwars:core:wins:daily",
    var description: String = "Daily wins",
    var requirement: Long = 25L,
)
{
    fun meets(player: UUID) = StatisticService
        .get(player) {
            statisticRead(
                StatisticID.fromExpected(statisticID)
            ) {
                scoreAndPosition()
            }
        }
        .thenApply { score ->
            (score?.score?.toLong() ?: 0L) >= requirement
        }

    fun progress(player: UUID) = StatisticService
        .get(player) {
            statisticRead(
                StatisticID.fromExpected(statisticID)
            ) {
                scoreAndPosition()
            }
        }
        .thenApply { score ->
            score?.score?.toLong()
        }
}
