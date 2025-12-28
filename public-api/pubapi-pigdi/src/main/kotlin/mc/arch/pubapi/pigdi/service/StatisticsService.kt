package mc.arch.pubapi.pigdi.service

import com.github.benmanes.caffeine.cache.Caffeine
import jakarta.annotation.PostConstruct
import mc.arch.pubapi.pigdi.dto.*
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*

/**
 * Service for fetching and caching game statistics.
 *
 * @author Subham
 * @since 12/27/24
 */
@Service
class StatisticsService(
    private val redisTemplate: StringRedisTemplate
)
{
    private val logger = LoggerFactory.getLogger(StatisticsService::class.java)
    // Cache for leaderboard queries (5 min TTL)
    private val leaderboardCache = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(5))
        .maximumSize(1000)
        .build<String, LeaderboardPage>()

    // Cache for player statistics (1 min TTL)
    private val playerStatsCache = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(1))
        .maximumSize(5000)
        .build<String, PlayerStatistics>()

    // Cache for statistic metadata (10 min TTL)
    private val statisticMetadataCache = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(10))
        .maximumSize(500)
        .build<String, StatisticDetail>()

    // Cache for discovered statistic IDs (1 min TTL - periodic Redis scan)
    private val statisticsListCache = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(1))
        .maximumSize(1)
        .build<String, List<StatisticMetadata>>()

    // Lifetime patterns to filter out (daily, weekly with date patterns)
    private val lifetimePatterns = listOf(
        Regex(".*:daily:.*"),
        Regex(".*:weekly:.*")
    )

    /**
     * Initialize statistics list eagerly on startup.
     */
    @PostConstruct
    fun init()
    {
        logger.info("Initializing statistics service - performing eager Redis scan...")
        refreshStatisticsList()
    }

    /**
     * Refresh the statistics list every minute.
     */
    @Scheduled(fixedRate = 60000)
    fun refreshStatisticsList()
    {
        try
        {
            val statistics = scanAndBuildStatisticsList()
            statisticsListCache.put("all", statistics)
            logger.info("Refreshed statistics list: ${statistics.size} statistics found")
        }
        catch (e: Exception)
        {
            logger.error("Failed to refresh statistics list", e)
        }
    }

    /**
     * Get all available statistic IDs (cached, refreshed every minute).
     */
    fun listAllStatistics(): StatisticsListResponse
    {
        val statistics = statisticsListCache.get("all") {
            scanAndBuildStatisticsList()
        } ?: emptyList()

        return StatisticsListResponse(
            statistics = statistics,
            count = statistics.size
        )
    }

    /**
     * Get detailed information about a specific statistic.
     */
    fun getStatisticDetail(statisticId: String): StatisticDetail?
    {
        return statisticMetadataCache.get(statisticId) {
            buildStatisticDetail(statisticId)
        }
    }

    /**
     * Get all statistics for a player by UUID.
     */
    fun getPlayerStatistics(uuid: UUID, filter: List<String>? = null): PlayerStatistics?
    {
        val cacheKey = "$uuid:${filter?.sorted()?.joinToString(",") ?: "all"}"

        return playerStatsCache.get(cacheKey) {
            fetchPlayerStatistics(uuid, filter)
        }
    }

    /**
     * Get all statistics for a player by username.
     */
    fun getPlayerStatisticsByUsername(username: String, filter: List<String>? = null): PlayerStatistics?
    {
        val uuid = resolveUsername(username) ?: return null
        return getPlayerStatistics(uuid, filter)
    }

    /**
     * Get a specific statistic value for a player.
     */
    fun getPlayerStatisticValue(uuid: UUID, statisticId: String): StatisticValue?
    {
        val redisKey = buildRedisKey(statisticId)
        val ops = redisTemplate.opsForZSet()

        val score = ops.score(redisKey, uuid.toString()) ?: return null
        val rank = ops.reverseRank(redisKey, uuid.toString()) ?: return null
        val totalPlayers = ops.size(redisKey) ?: 1

        val position = rank.toInt() + 1 // 1-indexed
        val percentile = (position.toDouble() / totalPlayers.toDouble()) * 100.0

        return StatisticValue(
            statisticId = statisticId,
            value = score.toLong(),
            position = position,
            percentile = percentile,
            totalPlayers = totalPlayers.toInt()
        )
    }

    /**
     * Get leaderboard for a statistic.
     */
    fun getLeaderboard(statisticId: String, page: Int, size: Int): LeaderboardPage?
    {
        val cacheKey = "$statisticId:$page:$size"

        return leaderboardCache.get(cacheKey) {
            fetchLeaderboard(statisticId, page, size)
        }
    }

    // ==================== Private Methods ====================

    private val statisticsKeyPrefix = "tropicpractice:playerstats:"

    /**
     * Dynamically scan Redis for all statistic keys.
     * Called at most once per minute due to caching.
     */
    private fun scanAndBuildStatisticsList(): List<StatisticMetadata>
    {
        val statistics = mutableListOf<StatisticMetadata>()

        // Scan Redis for all statistic keys
        val keys = scanRedisKeys("$statisticsKeyPrefix*")
        logger.debug("Found ${keys.size} raw Redis keys")

        for (key in keys)
        {
            // Filter out keys with daily/weekly lifetime patterns (containing date suffixes)
            if (lifetimePatterns.any { it.matches(key) })
            {
                continue
            }

            val statisticId = key.removePrefix(statisticsKeyPrefix)
            val metadata = parseStatisticId(statisticId)
            if (metadata != null)
            {
                statistics.add(metadata)
            }
        }

        return statistics.sortedBy { it.id }
    }

    /**
     * Scan Redis for keys matching a pattern.
     */
    private fun scanRedisKeys(pattern: String): Set<String>
    {
        val keys = mutableSetOf<String>()

        try
        {
            // Use SCAN for production-safe key iteration
            val connectionFactory = redisTemplate.connectionFactory
            if (connectionFactory != null)
            {
                val scanOptions = org.springframework.data.redis.core.ScanOptions.scanOptions()
                    .match(pattern)
                    .count(1000)
                    .build()

                connectionFactory.connection.use { connection ->
                    val cursor = connection.scan(scanOptions)
                    while (cursor.hasNext())
                    {
                        val keyBytes = cursor.next()
                        keys.add(String(keyBytes))
                    }
                    cursor.close()
                }
            }
        }
        catch (e: Exception)
        {
            logger.warn("SCAN failed, falling back to KEYS command: ${e.message}")
            // Fallback to KEYS command (less efficient but works)
            redisTemplate.keys(pattern)?.let { keys.addAll(it) }
        }

        // If SCAN returned nothing, try KEYS as fallback
        if (keys.isEmpty())
        {
            logger.debug("SCAN returned empty, trying KEYS command for pattern: $pattern")
            redisTemplate.keys(pattern)?.let { keys.addAll(it) }
        }

        return keys
    }

    /**
     * Parse a statistic ID into metadata.
     * Format: {type}:{kit}:{queueType}:{lifetime}
     */
    private fun parseStatisticId(statisticId: String): StatisticMetadata?
    {
        val parts = statisticId.split(":")
        if (parts.size != 4) return null

        val (type, kit, queueType, lifetime) = parts

        return StatisticMetadata(
            id = statisticId,
            type = type,
            kit = if (kit == "global") null else kit,
            queueType = if (queueType == "global") null else queueType,
            lifetime = lifetime,
            displayName = buildDisplayName(type, kit, queueType, lifetime),
            defaultValue = getDefaultValue(type)
        )
    }

    private fun buildStatisticDetail(statisticId: String): StatisticDetail?
    {
        val parts = statisticId.split(":")
        if (parts.size != 4) return null

        val (type, kit, queueType, lifetime) = parts
        val redisKey = buildRedisKey(statisticId)
        val totalPlayers = redisTemplate.opsForZSet().size(redisKey)?.toInt() ?: 0

        return StatisticDetail(
            id = statisticId,
            type = type,
            kit = if (kit == "global") null else kit,
            queueType = if (queueType == "global") null else queueType,
            lifetime = lifetime,
            displayName = buildDisplayName(type, kit, queueType, lifetime),
            description = buildDescription(type, kit, queueType, lifetime),
            defaultValue = getDefaultValue(type),
            reversed = type == "elo", // Higher is better
            totalTrackedPlayers = totalPlayers
        )
    }

    private fun fetchPlayerStatistics(uuid: UUID, filter: List<String>?): PlayerStatistics?
    {
        val username = resolveUuidToUsername(uuid) ?: return null
        val allStats = statisticsListCache.get("all") { scanAndBuildStatisticsList() } ?: emptyList()

        val filteredStats = if (filter != null)
        {
            allStats.filter { stat -> filter.any { stat.type.contains(it, ignoreCase = true) } }
        }
        else
        {
            allStats
        }

        val statisticValues = mutableMapOf<String, StatisticValue>()

        for (stat in filteredStats)
        {
            val value = getPlayerStatisticValue(uuid, stat.id)
            if (value != null)
            {
                statisticValues[stat.id] = value
            }
        }

        return PlayerStatistics(
            uuid = uuid.toString(),
            username = username,
            lastSeen = null, // TODO: Implement last seen tracking
            statistics = statisticValues
        )
    }

    private fun fetchLeaderboard(statisticId: String, page: Int, size: Int): LeaderboardPage?
    {
        val redisKey = buildRedisKey(statisticId)
        val ops = redisTemplate.opsForZSet()

        val totalPlayers = ops.size(redisKey)?.toInt() ?: return null
        if (totalPlayers == 0) return null

        val totalPages = (totalPlayers + size - 1) / size
        val start = page.toLong() * size
        val end = start + size - 1

        val entries = ops.reverseRangeWithScores(redisKey, start, end)?.mapIndexed { index, tuple ->
            val uuid = tuple.value ?: return@mapIndexed null
            val score = tuple.score?.toLong() ?: 0L
            val position = (start + index + 1).toInt()

            LeaderboardEntry(
                position = position,
                uuid = uuid,
                username = resolveUuidToUsername(UUID.fromString(uuid)) ?: uuid,
                value = score
            )
        }?.filterNotNull() ?: emptyList()

        return LeaderboardPage(
            statisticId = statisticId,
            page = page,
            size = size,
            totalPages = totalPages,
            totalPlayers = totalPlayers,
            entries = entries
        )
    }

    private fun buildRedisKey(statisticId: String): String
    {
        return "$statisticsKeyPrefix$statisticId"
    }

    private fun buildDisplayName(type: String, kit: String, queueType: String, lifetime: String): String
    {
        val kitName = if (kit == "global") "" else "${kit.replaceFirstChar { it.uppercase() }} "
        val queueName = if (queueType == "global") "" else "${queueType.replaceFirstChar { it.uppercase() }} "
        val lifetimeName = if (lifetime == "lifetime") "" else "${lifetime.replaceFirstChar { it.uppercase() }} "
        val typeName = type.replaceFirstChar { it.uppercase() }

        return "${lifetimeName}${queueName}${kitName}$typeName".trim()
    }

    private fun buildDescription(type: String, kit: String, queueType: String, lifetime: String): String
    {
        val kitDesc = if (kit == "global") "all kits" else "the $kit kit"
        val queueDesc = if (queueType == "global") "all modes" else "$queueType matches"
        val lifetimeDesc = when (lifetime)
        {
            "daily" -> "today"
            "weekly" -> "this week"
            else -> "all time"
        }

        return "${type.replaceFirstChar { it.uppercase() }} for $kitDesc in $queueDesc ($lifetimeDesc)"
    }

    private fun getDefaultValue(type: String): Long
    {
        return when (type)
        {
            "elo" -> 1000L
            else -> 0L
        }
    }

    private fun resolveUsername(username: String): UUID?
    {
        // Look up in Redis UUID cache (hash: DataStore:UuidCache:Username)
        val uuid = redisTemplate.opsForHash<String, String>().get("DataStore:UuidCache:Username", username)
        return uuid?.let {
            try { UUID.fromString(it) } catch (e: Exception) { null }
        }
    }

    private fun resolveUuidToUsername(uuid: UUID): String?
    {
        // Look up in Redis UUID cache (hash: DataStore:UuidCache:UUID)
        return redisTemplate.opsForHash<String, String>().get("DataStore:UuidCache:UUID", uuid.toString())
    }
}
