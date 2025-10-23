package gg.tropic.practice.metrics.collectors

import dev.cubxity.plugins.metrics.api.metric.collector.Collector
import dev.cubxity.plugins.metrics.api.metric.data.GaugeMetric
import gg.tropic.practice.games.GameService

/**
 * @author GrowlyX
 * @since 3/2/2024
 */
object GameDistributionCollector : Collector
{
    override fun collect() = listOf(
        GaugeMetric(
            "minigame_match_spectator_count", mapOf(),
            GameService.gameMappings.values.sumOf { it.expectedSpectators.size }
        )
    ) +  GameService.gameMappings.values
        .groupBy { it.minigameType() ?: "duels" }
        .map { entry ->
            listOf(
                GaugeMetric(
                    "minigame_match_minigame_count_grouped",
                    mapOf("type" to entry.key),
                    entry.value.size
                ),
                GaugeMetric(
                    "minigame_match_minigame_onlineplayers_grouped",
                    mapOf("type" to entry.key),
                    entry.value.sumOf { it.toBukkitPlayers().size }
                )
            )
        }
        .flatten()
}
