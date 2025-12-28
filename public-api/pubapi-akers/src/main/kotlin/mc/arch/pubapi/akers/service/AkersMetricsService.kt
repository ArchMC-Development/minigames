package mc.arch.pubapi.akers.service

import gg.scala.commons.ScalaCommons
import java.util.concurrent.TimeUnit

/**
 * Service for tracking API key usage metrics in Redis.
 *
 * @author Subham
 * @since 12/27/24
 */
object AkersMetricsService
{
    private const val REQUESTS_PREFIX = "pigdi:metrics:requests:"

    private fun redis() = ScalaCommons.bundle().globals().redis().sync()

    /**
     * Get daily request count for an API key.
     */
    fun getDailyRequests(apiKeyToken: String): Long
    {
        val today = java.time.LocalDate.now().toString()
        val key = "$REQUESTS_PREFIX$apiKeyToken:$today"
        return redis().get(key)?.toLongOrNull() ?: 0L
    }

    /**
     * Get total requests for an API key over the last N days.
     */
    fun getTotalRequests(apiKeyToken: String, days: Int = 7): Long
    {
        var total = 0L
        val startDate = java.time.LocalDate.now().minusDays((days - 1).toLong())

        for (i in 0 until days)
        {
            val date = startDate.plusDays(i.toLong()).toString()
            val key = "$REQUESTS_PREFIX$apiKeyToken:$date"
            total += redis().get(key)?.toLongOrNull() ?: 0L
        }

        return total
    }
}
