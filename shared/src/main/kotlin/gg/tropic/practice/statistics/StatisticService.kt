package gg.tropic.practice.statistics

import gg.scala.commons.ScalaCommons
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.tropic.practice.kit.KitService
import gg.tropic.practice.kit.feature.FeatureFlag
import gg.tropic.practice.profile.PracticeProfile
import gg.tropic.practice.profile.PracticeProfileService
import gg.tropic.practice.queue.QueueType
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Subham
 * @since 6/18/25
 */
@Service
object StatisticService
{
    private var statistics = mapOf<String, Statistic>()
    private var customStatisticProviders = mutableListOf<() -> List<Statistic>>()

    fun provideCustomStatistics(provider: () -> List<Statistic>)
    {
        customStatisticProviders += provider
        statistics = provideKitStatistics()
    }

    fun statisticBy(id: String) = statistics[id]
    fun trackedStatistics() = statistics.values

    @Configure
    fun configure()
    {
        statistics = provideKitStatistics()

        KitService.onReload {
            statistics = provideKitStatistics()
        }
    }

    fun <T> get(
        player: UUID,
        closure: PracticeProfile.() -> T
    ): CompletableFuture<T?>
    {
        val local = PracticeProfileService.find(player)
        if (local != null)
        {
            return CompletableFuture.completedFuture(local.closure())
        }

        return PracticeProfileService
            .loadCopyOf(player)
            .thenApply { profile ->
                profile?.closure()
            }
    }

    fun trackStatistic(profile: PracticeProfile, id: String, use: Statistic.() -> Unit)
    {
        val statistic = statistics[id]
            ?: return

        profile.track(id)
        profile.track(statistic, use)
    }

    fun trackStatistic(profile: PracticeProfile, statistic: Statistic, use: Statistic.() -> Unit)
    {
        profile.track(statistic.id.toId())
        profile.track(statistic, use)
    }

    fun <T> statistic(id: String, use: Statistic.() -> T) = statistics[id]?.use()
    fun <T> statistic(id: StatisticID, use: Statistic.() -> T) = statistics[id.toId()]?.use()

    fun update(
        player: UUID,
        closure: PracticeProfile.() -> Unit
    ): CompletableFuture<List<StatisticChange>>
    {
        val local = PracticeProfileService.find(player)
        if (local != null)
        {
            if (local.inImmutableMode())
            {
                /**
                 * TODO: potentially go through and compute the
                 *  changes but return empty sets
                 */
                return CompletableFuture.completedFuture(listOf())
            }

            val oldPositions = ConcurrentHashMap<String, LeaderboardPosition>()
            local.trackedStatisticChangeSet = mutableSetOf()
            local.trackedStatisticChangeMutations = mutableMapOf()

            return CompletableFuture
                .runAsync {
                    local.closure()
                    local.trackedStatisticChangeSet.toSet().forEach { statisticId ->
                        statistics[statisticId]?.let { statistic ->
                            with(local) {
                                val scoreAndPosition = statistic.scoreAndPosition()
                                oldPositions[statisticId] = LeaderboardPosition(
                                    uniqueId = local.identifier,
                                    score = scoreAndPosition.score.toLong(),
                                    position = scoreAndPosition.value.toLong()
                                )
                            }
                        }
                    }

                    local.trackedStatisticChangeMutations.toMap().entries
                        .forEach { mut ->
                            mut.value(mut.key)
                        }
                }
                .thenCompose {
                    local.save().thenApply {
                        // Convert tracked statistic changes to StatisticChange objects
                        local.trackedStatisticChangeSet.toSet().mapNotNull { statisticId ->
                            val statistic = statistics[statisticId]
                            val oldPosition = oldPositions[statisticId]

                            if (statistic != null && oldPosition != null)
                            {
                                with(local) {
                                    val newScoreAndPosition = statistic.scoreAndPosition()
                                    val newPosition = LeaderboardPosition(
                                        uniqueId = local.identifier,
                                        score = newScoreAndPosition.score.toLong(),
                                        position = newScoreAndPosition.value.toLong()
                                    )

                                    val nextPositionValue = (newPosition.position) - 1
                                    val nextPosition = if (nextPositionValue < 0)
                                    {
                                        null
                                    } else
                                    {
                                        val scoreResult = ScalaCommons.bundle().globals().redis().sync()
                                            .zrevrangeWithScores(
                                                statistic.id.toRedisKey(),
                                                nextPositionValue, nextPositionValue
                                            )
                                            .firstOrNull()

                                        scoreResult?.let { scoredValue ->
                                            LeaderboardPosition(
                                                uniqueId = UUID.fromString(scoredValue.value),
                                                score = scoredValue.score.toLong(),
                                                position = nextPositionValue
                                            )
                                        }
                                    }

                                    StatisticChange(
                                        id = statisticId,
                                        old = oldPosition,
                                        new = newPosition,
                                        next = nextPosition
                                    )
                                }
                            } else null
                        }
                    }
                }
                .whenComplete { t, u ->
                    u?.printStackTrace()
                }
        }

        return PracticeProfileService
            .useAsyncThenSaveGlobally(player) {
                it.trackedStatisticChangeSet = mutableSetOf()
                it.trackedStatisticChangeMutations = mutableMapOf()

                // Capture old positions before changes
                val oldPositions = ConcurrentHashMap<String, LeaderboardPosition>()
                it.closure()
                it.trackedStatisticChangeSet.toSet().forEach { statisticId ->
                    statistics[statisticId]?.let { statistic ->
                        with(it) {
                            val scoreAndPosition = statistic.scoreAndPosition()
                            oldPositions[statisticId] = LeaderboardPosition(
                                uniqueId = it.identifier,
                                score = scoreAndPosition.score.toLong(),
                                position = scoreAndPosition.value.toLong()
                            )
                        }
                    }
                }

                it.trackedStatisticChangeMutations.toMap().entries
                    .forEach { mut ->
                        mut.value(mut.key)
                    }

                it.trackedStatisticChangeSet.toSet().mapNotNull { statisticId ->
                    val statistic = statistics[statisticId]
                    val oldPosition = oldPositions[statisticId]

                    if (statistic != null && oldPosition != null)
                    {
                        with(it) {
                            val newScoreAndPosition = statistic.scoreAndPosition()
                            val newPosition = LeaderboardPosition(
                                uniqueId = it.identifier,
                                score = newScoreAndPosition.score.toLong(),
                                position = newScoreAndPosition.value.toLong()
                            )

                            val nextPositionValue = (newPosition.position) - 1
                            val nextPosition = if (nextPositionValue < 0)
                            {
                                null
                            } else
                            {
                                val scoreResult = ScalaCommons.bundle().globals().redis().sync()
                                    .zrevrangeWithScores(
                                        statistic.id.toRedisKey(),
                                        nextPositionValue, nextPositionValue
                                    )
                                    .firstOrNull()

                                scoreResult?.let { scoredValue ->
                                    LeaderboardPosition(
                                        uniqueId = UUID.fromString(scoredValue.value),
                                        score = scoredValue.score.toLong(),
                                        position = nextPositionValue
                                    )
                                }
                            }

                            StatisticChange(
                                id = statisticId,
                                old = oldPosition,
                                new = newPosition,
                                next = nextPosition
                            )
                        }
                    } else null
                }
            }
            .whenComplete { t, u ->
                u?.printStackTrace()
            }
    }

    private fun provideKitStatistics(): Map<String, Statistic>
    {
        val statistics = mutableMapOf<String, Statistic>()
        statistics.putAll(customStatisticProviders.flatMap { it().map { key -> key.id.toId() to key } })

        listOf(*KitService.cached().kits.values.toTypedArray(), null).forEach { kit ->
            TrackedKitStatistic.entries.forEach { statistic ->
                listOf(QueueType.Ranked, QueueType.Casual, null)
                    .forEach { queueType ->
                        if (kit != null && statistic == TrackedKitStatistic.ELO)
                        {
                            if (!kit.features(FeatureFlag.Ranked))
                            {
                                return@forEach
                            }
                        }

                        val id = statisticIdFrom(statistic) {
                            if (queueType != null)
                            {
                                queueType(queueType)
                            }
                            kit(kit)
                        }

                        statistics[id.toId()] = IncrementalStatistic(
                            id,
                            defaultValue = if (statistic == TrackedKitStatistic.ELO)
                                1000L else 0L
                        )

                        statistic.timeSensitive.forEach { lifetime ->
                            val newId = statisticIdFrom(statistic) {
                                if (queueType != null)
                                {
                                    queueType(queueType)
                                }

                                kit(kit)
                                lifetime(lifetime)
                            }

                            statistics[newId.toId()] = ExpiringStatistic(
                                id = newId,
                                lifetime = lifetime
                            )
                        }
                    }
            }
        }

        return statistics.toMap()
    }
}
