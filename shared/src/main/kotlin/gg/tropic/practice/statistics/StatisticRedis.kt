package gg.tropic.practice.statistics

import gg.scala.commons.ScalaCommons
import gg.tropic.practice.namespace
import java.util.UUID

/**
 * @author Subham
 * @since 6/18/25
 */
object StatisticRedis
{
    fun scoreAndPositionOf(player: UUID, id: String, reversed: Boolean) = ScalaCommons
        .bundle()
        .globals()
        .redis()
        .sync()
        .let {
            if (reversed)
            {
                it.zrevrankWithScore(
                    id,
                    player.toString()
                )
            } else
            {
                it.zrankWithScore(
                    id,
                    player.toString()
                )
            }
        }

    fun save(player: UUID, id: String, value: Long) = ScalaCommons
        .bundle()
        .globals()
        .redis()
        .sync()
        .zadd(
            id,
            value.toDouble(),
            player.toString()
        )

    fun exists(id: String) = ScalaCommons
        .bundle()
        .globals()
        .redis()
        .sync()
        .exists(id)

    fun incr(player: UUID, id: String, by: Long) = ScalaCommons
        .bundle()
        .globals()
        .redis()
        .sync()
        .zincrby(
            id,
            by.toDouble(),
            player.toString()
        )

    fun save(player: UUID, id: String, value: Long, lifetime: StatisticLifetime)
    {
        val redis = ScalaCommons.bundle().globals().redis().sync()
        redis.zadd(id, value.toDouble(), player.toString())
        redis.expireat(id, lifetime.getTimeAtReset())
    }

    fun incr(player: UUID, id: String, by: Long, lifetime: StatisticLifetime)
    {
        val redis = ScalaCommons.bundle().globals().redis().sync()
        redis.zincrby(id, by.toDouble(), player.toString())
        redis.expireat(id, lifetime.getTimeAtReset())
    }

    fun getTopPlayers(id: String, limit: Int = 10, reversed: Boolean = true) = ScalaCommons
        .bundle()
        .globals()
        .redis()
        .sync()
        .let { sync ->
            if (reversed)
            {
                sync.zrevrangeWithScores(
                    id,
                    0,
                    (limit - 1).toLong()
                )
            } else
            {
                sync.zrangeWithScores(
                    id,
                    0,
                    (limit - 1).toLong()
                )
            }
        }
}
