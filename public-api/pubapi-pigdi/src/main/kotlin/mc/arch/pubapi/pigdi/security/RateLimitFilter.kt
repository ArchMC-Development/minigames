package mc.arch.pubapi.pigdi.security

import com.google.gson.Gson
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mc.arch.pubapi.pigdi.dto.ErrorResponse
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration

/**
 * Filter for rate limiting API requests using Redis sliding window.
 *
 * @author Subham
 * @since 12/27/24
 */
@Component
class RateLimitFilter(
    private val redisTemplate: StringRedisTemplate,
    private val gson: Gson
) : OncePerRequestFilter()
{
    companion object
    {
        const val REQUESTS_PER_MINUTE = 100
        const val WINDOW_SECONDS = 60L

        const val HEADER_LIMIT = "X-RateLimit-Limit"
        const val HEADER_REMAINING = "X-RateLimit-Remaining"
        const val HEADER_RESET = "X-RateLimit-Reset"
        const val HEADER_RETRY_AFTER = "Retry-After"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    )
    {
        // Skip rate limiting for non-API endpoints
        val path = request.requestURI
        if (!path.startsWith("/v1/"))
        {
            filterChain.doFilter(request, response)
            return
        }

        val apiKey = request.getAttribute(ApiKeyAuthFilter.API_KEY_ATTRIBUTE) as? String

        if (apiKey == null)
        {
            // Auth filter should have already rejected this
            filterChain.doFilter(request, response)
            return
        }

        val rateLimitKey = "akers:ratelimit:$apiKey"
        val ops = redisTemplate.opsForValue()

        // Increment request count
        val currentCount = ops.increment(rateLimitKey) ?: 1L

        // Set expiry on first request in window
        if (currentCount == 1L)
        {
            redisTemplate.expire(rateLimitKey, Duration.ofSeconds(WINDOW_SECONDS))
        }

        val ttl = redisTemplate.getExpire(rateLimitKey)
        val resetTime = System.currentTimeMillis() / 1000 + ttl
        val remaining = (REQUESTS_PER_MINUTE - currentCount).coerceAtLeast(0)

        // Always add rate limit headers
        response.setHeader(HEADER_LIMIT, REQUESTS_PER_MINUTE.toString())
        response.setHeader(HEADER_REMAINING, remaining.toString())
        response.setHeader(HEADER_RESET, resetTime.toString())

        // Check if rate limited
        if (currentCount > REQUESTS_PER_MINUTE)
        {
            response.setHeader(HEADER_RETRY_AFTER, ttl.toString())
            sendRateLimitError(response)
            return
        }

        // Track daily metrics async
        incrementDailyMetrics(apiKey)

        filterChain.doFilter(request, response)
    }

    private fun incrementDailyMetrics(apiKey: String)
    {
        try
        {
            val today = java.time.LocalDate.now().toString()
            // Use a cleaner key format - extract just the unique part of the API key
            val keyIdentifier = apiKey.removePrefix("amc-akers_")
            val metricsKey = "pigdi:metrics:daily:$keyIdentifier:$today"

            val currentCount = redisTemplate.opsForValue().increment(metricsKey) ?: 1L

            // Only set expiry on first request of the day (when count is 1)
            if (currentCount == 1L)
            {
                redisTemplate.expire(metricsKey, Duration.ofDays(7))
            }

            // Also track total requests for the API key (all-time counter)
            val totalKey = "pigdi:metrics:total:$keyIdentifier"
            redisTemplate.opsForValue().increment(totalKey)
        }
        catch (e: Exception)
        {
            // Don't let metrics tracking failures affect the request
            logger.warn("Failed to track API key metrics", e)
        }
    }

    private fun sendRateLimitError(response: HttpServletResponse)
    {
        response.status = 429 // Too Many Requests
        response.contentType = "application/json"
        response.writer.write(
            gson.toJson(
                ErrorResponse(
                    error = "RATE_LIMIT_EXCEEDED",
                    message = "Rate limit of $REQUESTS_PER_MINUTE requests per minute exceeded",
                    timestamp = System.currentTimeMillis()
                )
            )
        )
    }
}
