package gg.tropic.practice.statistics

import org.bson.Document
import java.util.*

/**
 * @author Subham
 * @since 6/23/25
 */
object RawStatisticCRUD
{
    fun set(profile: Document, newValue: Long, id: StatisticID)
    {
        val identifier = UUID.fromString(profile.getString("identifier"))

        // Don't clutter the DB with random unmapped keys
        if (StatisticRedis.exists(id.toRedisKey()) == 0L)
        {
            return
        }

        StatisticRedis.save(identifier, id.toRedisKey(), newValue)

        val statistics = profile.get("statistics", Document::class.java)
        if (statistics == null)
        {
            profile.put("statistics", Document().append(id.toId(), newValue.toString()))
            return
        }

        statistics.put(id.toId(), newValue.toString())
        profile.put("statistics", statistics)
    }

    fun add(profile: Document, addValue: Long, id: StatisticID)
    {
        val identifier = UUID.fromString(profile.getString("identifier"))

        // Don't clutter the DB with random unmapped keys
        if (StatisticRedis.exists(id.toRedisKey()) == 0L)
        {
            return
        }

        val statistics = profile.get("statistics", Document::class.java)
        val currentValue = if (statistics != null && statistics.containsKey(id.toId()))
        {
            statistics.getString(id.toId()).toLongOrNull() ?: 0L
        } else {
            0L
        }

        val newValue = currentValue + addValue
        StatisticRedis.save(identifier, id.toRedisKey(), newValue)

        if (statistics == null)
        {
            profile.put("statistics", Document().append(id.toId(), newValue.toString()))
            return
        }

        statistics.put(id.toId(), newValue.toString())
        profile.put("statistics", statistics)
    }
}
