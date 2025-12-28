package mc.arch.pubapi.pigdi.dto

/**
 * Response for BalTop endpoint (map of currency to leaderboard).
 * This is a schema helper for Swagger documentation.
 */
typealias BalTopMapResponse = Map<String, BalTopResponse>

/**
 * Response for BalTop endpoint for a single currency.
 */
data class BalTopResponse(
    val entries: List<BalTopEntry>,
    val count: Int
)

/**
 * Individual BalTop entry with player info.
 */
data class BalTopEntry(
    val position: Int,
    val uuid: String,
    val username: String,
    val balance: Long
)

/**
 * Economy profile response for a player.
 */
data class EconomyProfileResponse(
    val uuid: String,
    val username: String,
    val balances: Map<String, Long>,
    val statistics: Map<String, CurrencyStatistics>
)

/**
 * Currency transaction statistics.
 */
data class CurrencyStatistics(
    val spent: Long,
    val made: Long,
    val wagered: Long
)
