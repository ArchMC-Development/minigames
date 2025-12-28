package mc.arch.pubapi.pigdi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import mc.arch.pubapi.pigdi.dto.ErrorResponse
import mc.arch.pubapi.pigdi.dto.GuildListResponse
import mc.arch.pubapi.pigdi.dto.GuildSearchResponse
import mc.arch.pubapi.pigdi.service.GuildService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * REST controller for Guild endpoints.
 *
 * Provides read-only endpoints for accessing guild data.
 * Guild data is cached and refreshed every minute.
 *
 * @author Subham
 * @since 12/28/24
 */
@RestController
@RequestMapping("/v1/guilds")
@Tag(
    name = "Guilds",
    description = """
        Read-only endpoints for accessing guild information.
        
        Guild data is cached in memory and refreshed every minute.
    """
)
class GuildController(
    private val guildService: GuildService
)
{
    @GetMapping
    @Operation(
        summary = "List all guilds",
        description = "Returns a paginated list of all guilds."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Paginated guild list"),
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
    fun getAllGuilds(
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
        val totalGuilds = guildService.getTotalGuildCount()
        val totalPages = if (totalGuilds == 0) 0 else (totalGuilds + clampedSize - 1) / clampedSize
        val guilds = guildService.getAllGuilds(page, clampedSize)

        return ResponseEntity.ok(
            GuildListResponse(
                page = page,
                size = clampedSize,
                totalPages = totalPages,
                totalGuilds = totalGuilds,
                guilds = guilds
            )
        )
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get guild by ID",
        description = "Returns a guild by its unique identifier (UUID)."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Guild information"),
            ApiResponse(
                responseCode = "401",
                description = "Invalid or missing API key",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Guild not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "429",
                description = "Rate limit exceeded",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    fun getGuildById(
        @Parameter(description = "Guild UUID")
        @PathVariable id: String
    ): ResponseEntity<Any>
    {
        val guild = guildService.getGuildById(id)
            ?: return ResponseEntity.status(404).body(
                ErrorResponse(
                    error = "GUILD_NOT_FOUND",
                    message = "No guild found with ID '$id'"
                )
            )

        return ResponseEntity.ok(guild)
    }

    @GetMapping("/player/uuid/{uuid}")
    @Operation(
        summary = "Get guild by player UUID",
        description = "Returns the guild that a player belongs to, by player UUID."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Guild information"),
            ApiResponse(
                responseCode = "400",
                description = "Invalid UUID format",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Invalid or missing API key",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Player not in a guild",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "429",
                description = "Rate limit exceeded",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    fun getGuildByPlayerUuid(
        @Parameter(description = "Player's Minecraft UUID")
        @PathVariable uuid: String
    ): ResponseEntity<Any>
    {
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

        val guild = guildService.getGuildByPlayerUuid(parsedUuid)
            ?: return ResponseEntity.status(404).body(
                ErrorResponse(
                    error = "NOT_IN_GUILD",
                    message = "Player '$uuid' is not in a guild"
                )
            )

        return ResponseEntity.ok(guild)
    }

    @GetMapping("/player/username/{username}")
    @Operation(
        summary = "Get guild by player username",
        description = "Returns the guild that a player belongs to, by player username."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Guild information"),
            ApiResponse(
                responseCode = "401",
                description = "Invalid or missing API key",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Player not found or not in a guild",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "429",
                description = "Rate limit exceeded",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    fun getGuildByPlayerUsername(
        @Parameter(description = "Player's Minecraft username")
        @PathVariable username: String
    ): ResponseEntity<Any>
    {
        val guild = guildService.getGuildByPlayerUsername(username)
            ?: return ResponseEntity.status(404).body(
                ErrorResponse(
                    error = "NOT_IN_GUILD",
                    message = "Player '$username' was not found or is not in a guild"
                )
            )

        return ResponseEntity.ok(guild)
    }

    @GetMapping("/search/name")
    @Operation(
        summary = "Search guilds by name",
        description = "Searches for guilds with names containing the query string (case-insensitive). Returns max 50 results."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Search results"),
            ApiResponse(
                responseCode = "400",
                description = "Missing query parameter",
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
    fun searchGuildsByName(
        @Parameter(description = "Search query (case-insensitive)")
        @RequestParam q: String
    ): ResponseEntity<Any>
    {
        if (q.isBlank())
        {
            return ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "MISSING_QUERY",
                    message = "Search query 'q' is required"
                )
            )
        }

        val results = guildService.searchGuildsByName(q)

        return ResponseEntity.ok(
            GuildSearchResponse(
                query = q,
                searchType = "name",
                count = results.size,
                guilds = results
            )
        )
    }

    @GetMapping("/search/description")
    @Operation(
        summary = "Search guilds by description",
        description = "Searches for guilds with descriptions containing the query string (case-insensitive). Returns max 50 results."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Search results"),
            ApiResponse(
                responseCode = "400",
                description = "Missing query parameter",
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
    fun searchGuildsByDescription(
        @Parameter(description = "Search query (case-insensitive)")
        @RequestParam q: String
    ): ResponseEntity<Any>
    {
        if (q.isBlank())
        {
            return ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "MISSING_QUERY",
                    message = "Search query 'q' is required"
                )
            )
        }

        val results = guildService.searchGuildsByDescription(q)

        return ResponseEntity.ok(
            GuildSearchResponse(
                query = q,
                searchType = "description",
                count = results.size,
                guilds = results
            )
        )
    }
}
