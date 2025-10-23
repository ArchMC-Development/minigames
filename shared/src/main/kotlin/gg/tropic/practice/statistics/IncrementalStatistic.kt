package gg.tropic.practice.statistics

import gg.scala.commons.ScalaCommons
import gg.tropic.practice.leaderboards.LeaderboardEntry
import gg.tropic.practice.profile.PracticeProfile
import io.lettuce.core.ScoredValue
import java.util.*

/**
 * @author Subham
 * @since 6/18/25
 */
class IncrementalStatistic(
    override val id: StatisticID,
    override val defaultValue: Long = 0L,
    override val reversed: Boolean = true
) : Statistic
{
    override fun asyncLoadTopTenPlayers() = StatisticRedis
        .getTopPlayers(id.toRedisKey(), reversed = reversed)
        .filter { it.hasValue() }
        .map { entry ->
            LeaderboardEntry(
                uniqueId = UUID.fromString(entry.value),
                value = entry.score.toLong()
            )
        }

    context(PracticeProfile)
    override fun scoreAndPosition(): ScoredValue<Long>
    {
        val score = StatisticRedis.scoreAndPositionOf(identifier, id.toRedisKey(), reversed)
        if (score == null || !score.hasValue())
        {
            // Return -1, don't clutter the memory DB with default values
            return ScoredValue.just(defaultValue.toDouble(), -1)
//            set(defaultValue)
//            return StatisticRedis.scoreAndPositionOf(identifier, id.toRedisKey())
        }
        return score
    }

    context(PracticeProfile)
    override fun update(newValue: Long)
    {
        StatisticRedis.save(identifier, id.toRedisKey(), newValue)
        statistics()[id.toId()] = newValue
    }

    context(PracticeProfile)
    override fun percentile(): Float
    {
        val redis = ScalaCommons.bundle().globals().redis().sync()
        val redisKey = id.toRedisKey()
        val playerKey = identifier.toString()

        val playerRank = (if (reversed) redis.zrevrank(redisKey, playerKey) else redis.zrank(redisKey, playerKey)) ?: return 100.0f
        val totalPlayers = redis.zcard(redisKey)

        if (totalPlayers == 0L) return 100.0f

        return (((playerRank + 1).toDouble() / totalPlayers.toDouble()) * 100.0F)
            .toFloat()
            .coerceIn(0.0f, 100.0f)
    }

    context(PracticeProfile)
    override fun add(newValue: Long)
    {
        val redis = ScalaCommons.bundle().globals().redis().sync()
        val exists = redis.zscore(id.toRedisKey(), identifier.toString()) != null

        if (!exists)
        {
            StatisticRedis.save(identifier, id.toRedisKey(), defaultValue)
            StatisticRedis.incr(identifier, id.toRedisKey(), newValue)
        } else
        {
            StatisticRedis.incr(identifier, id.toRedisKey(), newValue)
        }

        statistics()[id.toId()] = (statistics()[id.toId()] ?: defaultValue) + newValue
    }

    context(PracticeProfile)
    override fun subtract(newValue: Long)
    {
        val redis = ScalaCommons.bundle().globals().redis().sync()
        val exists = redis.zscore(id.toRedisKey(), identifier.toString()) != null

        if (!exists)
        {
            StatisticRedis.save(identifier, id.toRedisKey(), defaultValue)
            StatisticRedis.incr(identifier, id.toRedisKey(), -newValue)
        } else
        {
            StatisticRedis.incr(identifier, id.toRedisKey(), -newValue)
        }

        statistics()[id.toId()] = (statistics()[id.toId()] ?: defaultValue) - newValue
    }
}
