package mc.arch.pubapi.pigdi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import mc.arch.pubapi.pigdi.dto.*
import mc.arch.pubapi.pigdi.model.UGCGamemode
import mc.arch.pubapi.pigdi.service.ClanLeaderboardService
import mc.arch.pubapi.pigdi.service.UGCStatisticsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * REST controller for UGC (User Generated Content) gamemode statistics.
 *
 * Provides endpoints for accessing statistics and leaderboards for UGC gamemodes:
 * - **trojan**: Lifesteal gamemode - PvP with health stealing mechanics
 * - **spartan**: Survival gamemode - Classic survival experience
 *
 * @author Subham
 * @since 12/28/24
 */
@RestController
@RequestMapping("/v1/ugc")
@Tag(
    name = "UGC Statistics",
    description = """
        Endpoints for accessing User Generated Content (UGC) gamemode statistics.
        
        Available gamemodes:
        - **trojan**: Lifesteal - PvP gamemode with health stealing mechanics
        - **spartan**: Survival - Classic survival experience
    """
)
class UGCController(
    private val ugcStatisticsService: UGCStatisticsService,
    private val clanLeaderboardService: ClanLeaderboardService
)
{
    @GetMapping("/{gamemode}/statistics")
    @Operation(
        summary = "List available statistics for a gamemode",
        description = """
            Returns all available statistic types that can be queried for the specified UGC gamemode.
            
            **Gamemodes:**
            - `trojan` - Lifesteal gamemode
            - `spartan` - Survival gamemode
        """
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "List of available statistics"),
            ApiResponse(
                responseCode = "400",
                description = "Invalid gamemode",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Invalid or missing API key",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "429",
                description = "Rate limit exceeded",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    fun listStatistics(
        @Parameter(
            description = "UGC gamemode (trojan = Lifesteal, spartan = Survival)",
            schema = Schema(allowableValues = ["trojan", "spartan"])
        )
        @PathVariable gamemode: String
    ): ResponseEntity<Any>
    {
        val ugcGamemode = UGCGamemode.fromApiName(gamemode)
            ?: return ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "INVALID_GAMEMODE",
                    message = "Invalid gamemode '$gamemode'. Valid values: trojan (Lifesteal), spartan (Survival)"
                )
            )

        val response = ugcStatisticsService.listAvailableStatistics(ugcGamemode)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{gamemode}/leaderboard/{statType}")
    @Operation(
        summary = "Get leaderboard for a statistic",
        description = """
            Returns a paginated leaderboard for a specific statistic in a UGC gamemode.
            
            **Gamemodes:**
            - `trojan` - Lifesteal gamemode
            - `spartan` - Survival gamemode
            
            **Common stat types:** kills, deaths, killstreak, killDeathRatio, blocksMined, blocksWalked, blocksPlaced
        """
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Leaderboard page"),
            ApiResponse(
                responseCode = "400",
                description = "Invalid gamemode or parameters",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Invalid or missing API key",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Statistic not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "429",
                description = "Rate limit exceeded",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    fun getLeaderboard(
        @Parameter(
            description = "UGC gamemode (trojan = Lifesteal, spartan = Survival)",
            schema = Schema(allowableValues = ["trojan", "spartan"])
        )
        @PathVariable gamemode: String,
        @Parameter(description = "Statistic type (e.g., kills, deaths, killstreak)")
        @PathVariable statType: String,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Results per page (max 100)")
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Any>
    {
        val ugcGamemode = UGCGamemode.fromApiName(gamemode)
            ?: return ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "INVALID_GAMEMODE",
                    message = "Invalid gamemode '$gamemode'. Valid values: trojan (Lifesteal), spartan (Survival)"
                )
            )

        if (page < 0)
        {
            return ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "INVALID_PAGE",
                    message = "Page number must be >= 0"
                )
            )
        }

        val clampedSize = size.coerceIn(1, 100)

        val leaderboard = ugcStatisticsService.getLeaderboard(ugcGamemode, statType, page, clampedSize)
            ?: return ResponseEntity.status(404).body(
                ErrorResponse(
                    error = "STATISTIC_NOT_FOUND",
                    message = "No statistic '$statType' found for gamemode '${ugcGamemode.displayName}'"
                )
            )

        return ResponseEntity.ok(leaderboard)
    }

    @GetMapping("/{gamemode}/players/uuid/{uuid}/statistics")
    @Operation(
        summary = "Get all statistics for a player by UUID",
        description = """
            Retrieves all tracked statistics for a player in a specific UGC gamemode using their Minecraft UUID.
            
            **Gamemodes:**
            - `trojan` - Lifesteal gamemode
            - `spartan` - Survival gamemode
        """
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Player statistics"),
            ApiResponse(
                responseCode = "400",
                description = "Invalid gamemode or UUID format",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Invalid or missing API key",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Player not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "429",
                description = "Rate limit exceeded",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    fun getPlayerStatsByUuid(
        @Parameter(
            description = "UGC gamemode (trojan = Lifesteal, spartan = Survival)",
            schema = Schema(allowableValues = ["trojan", "spartan"])
        )
        @PathVariable gamemode: String,
        @Parameter(description = "Minecraft UUID")
        @PathVariable uuid: String
    ): ResponseEntity<Any>
    {
        val ugcGamemode = UGCGamemode.fromApiName(gamemode)
            ?: return ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "INVALID_GAMEMODE",
                    message = "Invalid gamemode '$gamemode'. Valid values: trojan (Lifesteal), spartan (Survival)"
                )
            )

        val parsedUuid = try
        {
            UUID.fromString(uuid)
        }
        catch (e: Exception)
        {
            return ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "INVALID_UUID",
                    message = "Invalid UUID format: '$uuid'"
                )
            )
        }

        val stats = ugcStatisticsService.getPlayerStatistics(ugcGamemode, parsedUuid)
            ?: return ResponseEntity.status(404).body(
                ErrorResponse(
                    error = "PLAYER_NOT_FOUND",
                    message = "No statistics found for player '$uuid' in ${ugcGamemode.displayName}"
                )
            )

        return ResponseEntity.ok(stats)
    }

    @GetMapping("/{gamemode}/players/username/{username}/statistics")
    @Operation(
        summary = "Get all statistics for a player by username",
        description = """
            Retrieves all tracked statistics for a player in a specific UGC gamemode using their Minecraft username.
            
            **Gamemodes:**
            - `trojan` - Lifesteal gamemode
            - `spartan` - Survival gamemode
        """
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Player statistics"),
            ApiResponse(
                responseCode = "400",
                description = "Invalid gamemode",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Invalid or missing API key",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Player not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "429",
                description = "Rate limit exceeded",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    fun getPlayerStatsByUsername(
        @Parameter(
            description = "UGC gamemode (trojan = Lifesteal, spartan = Survival)",
            schema = Schema(allowableValues = ["trojan", "spartan"])
        )
        @PathVariable gamemode: String,
        @Parameter(description = "Minecraft username")
        @PathVariable username: String
    ): ResponseEntity<Any>
    {
        val ugcGamemode = UGCGamemode.fromApiName(gamemode)
            ?: return ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "INVALID_GAMEMODE",
                    message = "Invalid gamemode '$gamemode'. Valid values: trojan (Lifesteal), spartan (Survival)"
                )
            )

        val stats = ugcStatisticsService.getPlayerStatisticsByUsername(ugcGamemode, username)
            ?: return ResponseEntity.status(404).body(
                ErrorResponse(
                    error = "PLAYER_NOT_FOUND",
                    message = "No statistics found for player '$username' in ${ugcGamemode.displayName}"
                )
            )

        return ResponseEntity.ok(stats)
    }

    @GetMapping("/{gamemode}/players/uuid/{uuid}/statistics/{statType}")
    @Operation(
        summary = "Get a specific statistic for a player",
        description = """
            Retrieves a single statistic value with ranking information for a player.
            
            **Gamemodes:**
            - `trojan` - Lifesteal gamemode
            - `spartan` - Survival gamemode
        """
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Statistic value"),
            ApiResponse(
                responseCode = "400",
                description = "Invalid gamemode or UUID format",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Invalid or missing API key",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Player or statistic not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "429",
                description = "Rate limit exceeded",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    fun getPlayerSpecificStat(
        @Parameter(
            description = "UGC gamemode (trojan = Lifesteal, spartan = Survival)",
            schema = Schema(allowableValues = ["trojan", "spartan"])
        )
        @PathVariable gamemode: String,
        @Parameter(description = "Minecraft UUID")
        @PathVariable uuid: String,
        @Parameter(description = "Statistic type (e.g., kills, deaths, killstreak)")
        @PathVariable statType: String
    ): ResponseEntity<Any>
    {
        val ugcGamemode = UGCGamemode.fromApiName(gamemode)
            ?: return ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "INVALID_GAMEMODE",
                    message = "Invalid gamemode '$gamemode'. Valid values: trojan (Lifesteal), spartan (Survival)"
                )
            )

        val parsedUuid = try
        {
            UUID.fromString(uuid)
        }
        catch (e: Exception)
        {
            return ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "INVALID_UUID",
                    message = "Invalid UUID format: '$uuid'"
                )
            )
        }

        val value = ugcStatisticsService.getPlayerStatisticValue(ugcGamemode, parsedUuid, statType)
            ?: return ResponseEntity.status(404).body(
                ErrorResponse(
                    error = "NOT_FOUND",
                    message = "Statistic '$statType' not found for player '$uuid' in ${ugcGamemode.displayName}"
                )
            )

        return ResponseEntity.ok(value)
    }

    // ==================== Clan Leaderboard (Trojan/Lifesteal only) ====================

    @GetMapping("/trojan/clans")
    @Operation(
        summary = "Get top clans leaderboard",
        description = """
            Returns a paginated leaderboard of the top clans in the Lifesteal (trojan) gamemode,
            sorted by clan level (experience / 1000).
            
            **Note:** This endpoint is only available for the Lifesteal (trojan) gamemode.
        """
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Clan leaderboard page"),
            ApiResponse(
                responseCode = "400",
                description = "Invalid parameters",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Invalid or missing API key",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "429",
                description = "Rate limit exceeded",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    fun getClanLeaderboard(
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Results per page (max 100)")
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Any>
    {
        if (page < 0)
        {
            return ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "INVALID_PAGE",
                    message = "Page number must be >= 0"
                )
            )
        }

        val clampedSize = size.coerceIn(1, 100)
        val leaderboard = clanLeaderboardService.getLeaderboard(page, clampedSize)

        return ResponseEntity.ok(leaderboard)
    }
}
