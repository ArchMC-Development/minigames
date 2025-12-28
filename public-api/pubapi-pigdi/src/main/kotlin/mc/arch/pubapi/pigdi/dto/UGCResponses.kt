package mc.arch.pubapi.pigdi.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * UGC-specific statistic metadata.
 */
@Schema(description = "Metadata for a UGC statistic type")
data class UGCStatisticMetadata(
    @Schema(description = "Internal stat type name (e.g., 'kills', 'deaths')")
    val statType: String,

    @Schema(description = "Human-readable display name")
    val displayName: String,

    @Schema(description = "Description of what this statistic tracks")
    val description: String
)

/**
 * List of available UGC statistics for a gamemode.
 */
@Schema(description = "Response containing all available statistics for a UGC gamemode")
data class UGCStatisticsListResponse(
    @Schema(description = "The gamemode these statistics belong to")
    val gamemode: String,

    @Schema(description = "Display name of the gamemode")
    val gamemodeDisplayName: String,

    @Schema(description = "List of available statistic types")
    val statistics: List<UGCStatisticMetadata>,

    @Schema(description = "Total count of available statistics")
    val count: Int
)

/**
 * Individual UGC leaderboard entry.
 */
@Schema(description = "A single entry in a UGC leaderboard")
data class UGCLeaderboardEntry(
    @Schema(description = "Position on the leaderboard (1-indexed)")
    val position: Int,

    @Schema(description = "Player's Minecraft UUID")
    val uuid: String,

    @Schema(description = "Player's Minecraft username")
    val username: String,

    @Schema(description = "The statistic value")
    val value: Long
)

/**
 * Paginated UGC leaderboard response.
 */
@Schema(description = "Paginated leaderboard for a UGC statistic")
data class UGCLeaderboardPage(
    @Schema(description = "The gamemode for this leaderboard")
    val gamemode: String,

    @Schema(description = "The statistic type for this leaderboard")
    val statType: String,

    @Schema(description = "Current page number (0-indexed)")
    val page: Int,

    @Schema(description = "Number of entries per page")
    val size: Int,

    @Schema(description = "Total number of pages")
    val totalPages: Int,

    @Schema(description = "Total number of players with this statistic")
    val totalPlayers: Int,

    @Schema(description = "Leaderboard entries for this page")
    val entries: List<UGCLeaderboardEntry>
)

/**
 * Individual UGC statistic value for a player.
 */
@Schema(description = "A player's value for a specific UGC statistic")
data class UGCStatisticValue(
    @Schema(description = "The statistic type")
    val statType: String,

    @Schema(description = "The player's value for this statistic")
    val value: Long,

    @Schema(description = "Player's position on the leaderboard (1-indexed)")
    val position: Int,

    @Schema(description = "Player's percentile (higher is better)")
    val percentile: Double,

    @Schema(description = "Total players tracked for this statistic")
    val totalPlayers: Int
)

/**
 * All UGC statistics for a player in a specific gamemode.
 */
@Schema(description = "All UGC statistics for a player in a specific gamemode")
data class UGCPlayerStatistics(
    @Schema(description = "Player's Minecraft UUID")
    val uuid: String,

    @Schema(description = "Player's Minecraft username")
    val username: String,

    @Schema(description = "The gamemode these statistics are from")
    val gamemode: String,

    @Schema(description = "Display name of the gamemode")
    val gamemodeDisplayName: String,

    @Schema(description = "Map of statistic type to value")
    val statistics: Map<String, UGCStatisticValue>
)

// ==================== Clan Leaderboard DTOs ====================

/**
 * Individual clan leaderboard entry.
 */
@Schema(description = "A single entry in the clan leaderboard")
data class ClanLeaderboardEntry(
    @Schema(description = "Position on the leaderboard (1-indexed)")
    val position: Int,

    @Schema(description = "Clan's unique identifier (UUID)")
    val uuid: String,

    @Schema(description = "Clan's internal name")
    val name: String,

    @Schema(description = "Clan's display name")
    val displayName: String,

    @Schema(description = "Clan leader's Minecraft UUID")
    val leaderUuid: String,

    @Schema(description = "Clan leader's Minecraft username (if available)")
    val leaderUsername: String?,

    @Schema(description = "Clan level (experience / 1000, rounded to 3 decimal places)")
    val level: Double,

    @Schema(description = "Number of members in the clan")
    val memberCount: Int
)

/**
 * Paginated clan leaderboard response.
 */
@Schema(description = "Paginated clan leaderboard sorted by level")
data class ClanLeaderboardPage(
    @Schema(description = "Current page number (0-indexed)")
    val page: Int,

    @Schema(description = "Number of entries per page")
    val size: Int,

    @Schema(description = "Total number of pages")
    val totalPages: Int,

    @Schema(description = "Total number of clans")
    val totalClans: Int,

    @Schema(description = "Clan leaderboard entries for this page")
    val entries: List<ClanLeaderboardEntry>
)

// ==================== Playtime Leaderboard DTOs ====================

/**
 * Individual playtime leaderboard entry.
 */
@Schema(description = "A single entry in the playtime leaderboard")
data class PlaytimeLeaderboardEntry(
    @Schema(description = "Position on the leaderboard (1-indexed)")
    val position: Int,

    @Schema(description = "Player's Minecraft UUID")
    val uuid: String,

    @Schema(description = "Player's Minecraft username (if available)")
    val username: String?,

    @Schema(description = "Total playtime in seconds")
    val playtimeSeconds: Long
)

/**
 * Paginated playtime leaderboard response.
 */
@Schema(description = "Paginated playtime leaderboard for a UGC gamemode")
data class PlaytimeLeaderboardPage(
    @Schema(description = "The gamemode for this leaderboard")
    val gamemode: String,

    @Schema(description = "Display name of the gamemode")
    val gamemodeDisplayName: String,

    @Schema(description = "Current page number (0-indexed)")
    val page: Int,

    @Schema(description = "Number of entries per page")
    val size: Int,

    @Schema(description = "Total number of pages")
    val totalPages: Int,

    @Schema(description = "Total number of players in the leaderboard")
    val totalPlayers: Int,

    @Schema(description = "Leaderboard entries for this page")
    val entries: List<PlaytimeLeaderboardEntry>
)

// ==================== Lifesteal Profile DTOs ====================

/**
 * Item filter configuration (sanitized - no actual block/potion lists).
 */
@Schema(description = "Player's item filter configuration")
data class ItemFilterResponse(
    @Schema(description = "Whether the item filter is enabled")
    val enabled: Boolean,

    @Schema(description = "Filter type (Blacklist or Whitelist)")
    val type: String,

    @Schema(description = "Number of excluded blocks")
    val excludedBlockCount: Int,

    @Schema(description = "Number of excluded potions")
    val excludedPotionCount: Int
)

/**
 * Lifesteal player profile (sanitized - no locations or sensitive data).
 */
@Schema(description = "Lifesteal (Trojan) player profile with public statistics")
data class LifestealProfileResponse(
    @Schema(description = "Player's Minecraft UUID")
    val uuid: String,

    @Schema(description = "Player's Minecraft username (if available)")
    val username: String?,

    @Schema(description = "Current hearts (half-hearts, e.g., 17 = 8.5 hearts)")
    val hearts: Int?,

    @Schema(description = "Total playtime in seconds")
    val totalPlaytimeSeconds: Long,

    @Schema(description = "Number of rename tokens owned")
    val renameTokens: Int,

    @Schema(description = "Number of homes set")
    val homeCount: Int,

    @Schema(description = "Number of public homes")
    val publicHomeCount: Int,

    @Schema(description = "Item filter configuration")
    val itemFilter: ItemFilterResponse?,

    @Schema(description = "List of ignored tutorial tips")
    val ignoredTips: List<String>,

    @Schema(description = "Whether V1 homes have been migrated")
    val hasMigratedV1Homes: Boolean?,

    @Schema(description = "Whether V2 homes have been migrated")
    val hasMigratedV2Homes: Boolean?
)

