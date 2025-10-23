package gg.tropic.practice.metrics.collectors

import dev.cubxity.plugins.metrics.api.metric.collector.Collector
import dev.cubxity.plugins.metrics.api.metric.data.GaugeMetric
import gg.tropic.practice.games.GameService
import gg.tropic.practice.ugc.HostedWorldInstanceService

/**
 * @author GrowlyX
 * @since 3/2/2024
 */
object HostedWorldDistributionCollector : Collector
{
    override fun collect() = HostedWorldInstanceService.worldInstances()
        .groupBy { it.providerType }
        .map { entry ->
            listOf(
                GaugeMetric(
                    "hosted_worlds_active_players",
                    mapOf("type" to entry.key.name),
                    entry.value.sumOf { it.onlinePlayers().size }
                ),
            )
        }
        .flatten()
}
