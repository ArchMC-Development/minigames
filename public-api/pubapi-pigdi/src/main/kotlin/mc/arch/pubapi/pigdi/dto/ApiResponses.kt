package mc.arch.pubapi.pigdi.dto

/**
 * Standard error response format.
 */
data class ErrorResponse(
    val error: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Statistic metadata.
 */
data class StatisticMetadata(
    val id: String,
    val type: String,
    val kit: String?,
    val queueType: String?,
    val lifetime: String,
    val displayName: String,
    val defaultValue: Long
)

/**
 * Detailed statistic information.
 */
data class StatisticDetail(
    val id: String,
    val type: String,
    val kit: String?,
    val queueType: String?,
    val lifetime: String,
    val displayName: String,
    val description: String,
    val defaultValue: Long,
    val reversed: Boolean,
    val totalTrackedPlayers: Int
)

/**
 * List of statistics response.
 */
data class StatisticsListResponse(
    val statistics: List<StatisticMetadata>,
    val count: Int
)

/**
 * Player statistics response.
 */
data class PlayerStatistics(
    val uuid: String,
    val username: String,
    val lastSeen: Long?,
    val statistics: Map<String, StatisticValue>
)

/**
 * Individual statistic value with position.
 */
data class StatisticValue(
    val statisticId: String,
    val value: Long,
    val position: Int,
    val percentile: Double,
    val totalPlayers: Int
)

/**
 * Leaderboard page response.
 */
data class LeaderboardPage(
    val statisticId: String,
    val page: Int,
    val size: Int,
    val totalPages: Int,
    val totalPlayers: Int,
    val entries: List<LeaderboardEntry>
)

/**
 * Individual leaderboard entry.
 */
data class LeaderboardEntry(
    val position: Int,
    val uuid: String,
    val username: String,
    val value: Long
)
