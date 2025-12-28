package mc.arch.pubapi.pigdi.dto

import io.swagger.v3.oas.annotations.media.Schema

// ==================== Guild DTOs ====================

/**
 * Guild member in response.
 */
@Schema(description = "A member of a guild")
data class GuildMemberResponse(
    @Schema(description = "Member's Minecraft UUID")
    val uuid: String,

    @Schema(description = "Member's Minecraft username (if available)")
    val username: String?,

    @Schema(description = "Date when the member joined the guild")
    val joinedOn: String?,

    @Schema(description = "Role in the guild (Leader, Admin, Member, etc.)")
    val role: String
)

/**
 * Guild response.
 */
@Schema(description = "Guild information")
data class GuildResponse(
    @Schema(description = "Guild's unique identifier (UUID)")
    val id: String,

    @Schema(description = "Guild name")
    val name: String,

    @Schema(description = "Guild description")
    val description: String?,

    @Schema(description = "Date when the guild was created")
    val createdOn: String?,

    @Schema(description = "Guild creator/leader")
    val creator: GuildMemberResponse?,

    @Schema(description = "List of guild members (excluding creator)")
    val members: List<GuildMemberResponse>,

    @Schema(description = "Total member count including creator")
    val memberCount: Int,

    @Schema(description = "Whether all members can invite others")
    val allInvite: Boolean
)

/**
 * Paginated guild list response.
 */
@Schema(description = "Paginated list of guilds")
data class GuildListResponse(
    @Schema(description = "Current page number (0-indexed)")
    val page: Int,

    @Schema(description = "Number of entries per page")
    val size: Int,

    @Schema(description = "Total number of pages")
    val totalPages: Int,

    @Schema(description = "Total number of guilds")
    val totalGuilds: Int,

    @Schema(description = "List of guilds for this page")
    val guilds: List<GuildResponse>
)

/**
 * Guild search results response.
 */
@Schema(description = "Guild search results")
data class GuildSearchResponse(
    @Schema(description = "Search query that was used")
    val query: String,

    @Schema(description = "Search type (name or description)")
    val searchType: String,

    @Schema(description = "Number of results found (max 50)")
    val count: Int,

    @Schema(description = "Matching guilds")
    val guilds: List<GuildResponse>
)
