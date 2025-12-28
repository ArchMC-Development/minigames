package mc.arch.pubapi.pigdi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import mc.arch.pubapi.pigdi.dto.ErrorResponse
import mc.arch.pubapi.pigdi.dto.PlayerStatistics
import mc.arch.pubapi.pigdi.dto.StatisticValue
import mc.arch.pubapi.pigdi.service.StatisticsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * REST controller for player statistics endpoints.
 *
 * @author Subham
 * @since 12/27/24
 */
@RestController
@RequestMapping("/v1/players")
@Tag(name = "Players", description = "Endpoints for accessing player statistics")
class PlayersController(
    private val statisticsService: StatisticsService
)
{
    @GetMapping("/username/{username}/statistics")
    @Operation(
        summary = "Get all statistics for a player by username",
        description = "Retrieves all tracked statistics for a player using their Minecraft username"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Player statistics"),
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
    fun getStatsByUsername(
        @Parameter(description = "Minecraft username")
        @PathVariable username: String,
        @Parameter(description = "Filter statistics by type (comma-separated)")
        @RequestParam(required = false) filter: String?
    ): ResponseEntity<Any>
    {
        val filterList = filter?.split(",")?.map { it.trim() }

        val stats = statisticsService.getPlayerStatisticsByUsername(username, filterList)
            ?: return ResponseEntity.status(404).body(
                ErrorResponse(
                    error = "PLAYER_NOT_FOUND",
                    message = "No player found with username '$username'"
                )
            )

        return ResponseEntity.ok(stats)
    }

    @GetMapping("/uuid/{uuid}/statistics")
    @Operation(
        summary = "Get all statistics for a player by UUID",
        description = "Retrieves all tracked statistics for a player using their Minecraft UUID"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Player statistics"),
            ApiResponse(
                responseCode = "401",
                description = "Invalid or missing API key",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid UUID format",
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
    fun getStatsByUuid(
        @Parameter(description = "Minecraft UUID")
        @PathVariable uuid: String,
        @Parameter(description = "Filter statistics by type (comma-separated)")
        @RequestParam(required = false) filter: String?
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

        val filterList = filter?.split(",")?.map { it.trim() }

        val stats = statisticsService.getPlayerStatistics(parsedUuid, filterList)
            ?: return ResponseEntity.status(404).body(
                ErrorResponse(
                    error = "PLAYER_NOT_FOUND",
                    message = "No player found with UUID '$uuid'"
                )
            )

        return ResponseEntity.ok(stats)
    }

    @GetMapping("/uuid/{uuid}/statistics/{statisticId}")
    @Operation(
        summary = "Get a specific statistic value for a player",
        description = "Retrieves a single statistic value with position and percentile"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Single statistic value"),
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
    fun getSpecificStat(
        @Parameter(description = "Minecraft UUID")
        @PathVariable uuid: String,
        @Parameter(description = "Statistic ID")
        @PathVariable statisticId: String
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

        val value = statisticsService.getPlayerStatisticValue(parsedUuid, statisticId)
            ?: return ResponseEntity.status(404).body(
                ErrorResponse(
                    error = "NOT_FOUND",
                    message = "Statistic '$statisticId' not found for player '$uuid'"
                )
            )

        return ResponseEntity.ok(value)
    }
}
