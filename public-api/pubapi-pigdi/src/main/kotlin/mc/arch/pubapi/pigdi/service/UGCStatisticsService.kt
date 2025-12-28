package mc.arch.pubapi.pigdi.service

import com.github.benmanes.caffeine.cache.Caffeine
import mc.arch.pubapi.pigdi.dto.*
import mc.arch.pubapi.pigdi.model.UGCGamemode
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*

/**
 * Service for fetching UGC (User Generated Content) gamemode statistics.
 *
 * Handles statistics stored in Redis under the pattern:
 * {gamemode}:statistics:{statType}
 *
 * @author Subham
 * @since 12/28/24
 */
@Service
class UGCStatisticsService(
    private val redisTemplate: StringRedisTemplate
)
{
    private val logger = LoggerFactory.getLogger(UGCStatisticsService::class.java)

    // Cache for leaderboard queries (5 min TTL)
    private val leaderboardCache = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(5))
        .maximumSize(500)
        .build<String, UGCLeaderboardPage>()

    // Cache for player statistics (1 min TTL)
    private val playerStatsCache = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(1))
        .maximumSize(2000)
        .build<String, UGCPlayerStatistics>()

    // Known UGC stat types (discovered from Redis structure)
    private val knownStatTypes = listOf(
        UGCStatisticMetadata("kills", "Kills", "Total player kills"),
        UGCStatisticMetadata("deaths", "Deaths", "Total deaths"),
        UGCStatisticMetadata("killstreak", "Kill Streak", "Highest kill streak achieved"),
        UGCStatisticMetadata("killDeathRatio", "K/D Ratio", "Kill to death ratio"),
        UGCStatisticMetadata("blocksMined", "Blocks Mined", "Total blocks mined"),
        UGCStatisticMetadata("blocksWalked", "Blocks Walked", "Total distance walked in blocks"),
        UGCStatisticMetadata("blocksPlaced", "Blocks Placed", "Total blocks placed")
    )

    /**
     * List available statistics for a UGC gamemode.
     */
    fun listAvailableStatistics(gamemode: UGCGamemode): UGCStatisticsListResponse
    {
        // Check which stat types actually exist in Redis for this gamemode
        val availableStats = knownStatTypes.filter { stat ->
            val key = buildRedisKey(gamemode, stat.statType)
            redisTemplate.hasKey(key)
        }

        return UGCStatisticsListResponse(
            gamemode = gamemode.name.lowercase(),
            gamemodeDisplayName = gamemode.displayName,
            statistics = availableStats,
            count = availableStats.size
        )
    }

    /**
     * Get leaderboard for a specific statistic in a gamemode.
     */
    fun getLeaderboard(gamemode: UGCGamemode, statType: String, page: Int, size: Int): UGCLeaderboardPage?
    {
        val cacheKey = "${gamemode.name}:$statType:$page:$size"

        return leaderboardCache.get(cacheKey) {
            fetchLeaderboard(gamemode, statType, page, size)
        }
    }

    /**
     * Get all statistics for a player in a gamemode by UUID.
     */
    fun getPlayerStatistics(gamemode: UGCGamemode, uuid: UUID): UGCPlayerStatistics?
    {
        val cacheKey = "${gamemode.name}:$uuid"

        return playerStatsCache.get(cacheKey) {
            fetchPlayerStatistics(gamemode, uuid)
        }
    }

    /**
     * Get all statistics for a player by username.
     */
    fun getPlayerStatisticsByUsername(gamemode: UGCGamemode, username: String): UGCPlayerStatistics?
    {
        val uuid = resolveUsername(username) ?: return null
        return getPlayerStatistics(gamemode, uuid)
    }

    /**
     * Get a specific statistic value for a player.
     */
    fun getPlayerStatisticValue(gamemode: UGCGamemode, uuid: UUID, statType: String): UGCStatisticValue?
    {
        val redisKey = buildRedisKey(gamemode, statType)
        val ops = redisTemplate.opsForZSet()

        val score = ops.score(redisKey, uuid.toString()) ?: return null
        val rank = ops.reverseRank(redisKey, uuid.toString()) ?: return null
        val totalPlayers = ops.size(redisKey) ?: 1

        val position = rank.toInt() + 1 // 1-indexed
        val percentile = ((totalPlayers - position + 1).toDouble() / totalPlayers.toDouble()) * 100.0

        return UGCStatisticValue(
            statType = statType,
            value = score.toLong(),
            position = position,
            percentile = percentile,
            totalPlayers = totalPlayers.toInt()
        )
    }

    // ==================== Private Methods ====================

    private fun fetchLeaderboard(gamemode: UGCGamemode, statType: String, page: Int, size: Int): UGCLeaderboardPage?
    {
        val redisKey = buildRedisKey(gamemode, statType)
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

            UGCLeaderboardEntry(
                position = position,
                uuid = uuid,
                username = resolveUuidToUsername(UUID.fromString(uuid)) ?: uuid,
                value = score
            )
        }?.filterNotNull() ?: emptyList()

        return UGCLeaderboardPage(
            gamemode = gamemode.name.lowercase(),
            statType = statType,
            page = page,
            size = size,
            totalPages = totalPages,
            totalPlayers = totalPlayers,
            entries = entries
        )
    }

    private fun fetchPlayerStatistics(gamemode: UGCGamemode, uuid: UUID): UGCPlayerStatistics?
    {
        val username = resolveUuidToUsername(uuid) ?: return null

        val statisticValues = mutableMapOf<String, UGCStatisticValue>()

        for (stat in knownStatTypes)
        {
            val value = getPlayerStatisticValue(gamemode, uuid, stat.statType)
            if (value != null)
            {
                statisticValues[stat.statType] = value
            }
        }

        // Return null if player has no stats
        if (statisticValues.isEmpty()) return null

        return UGCPlayerStatistics(
            uuid = uuid.toString(),
            username = username,
            gamemode = gamemode.name.lowercase(),
            gamemodeDisplayName = gamemode.displayName,
            statistics = statisticValues
        )
    }

    /**
     * Build the Redis key for a UGC statistic.
     * Format: {redisPrefix}:statistics:{statType}
     */
    private fun buildRedisKey(gamemode: UGCGamemode, statType: String): String
    {
        return "${gamemode.redisPrefix}:statistics:$statType"
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
