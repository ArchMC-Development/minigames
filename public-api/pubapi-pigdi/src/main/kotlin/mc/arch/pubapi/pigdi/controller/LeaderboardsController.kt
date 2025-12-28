package mc.arch.pubapi.pigdi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import mc.arch.pubapi.pigdi.dto.ErrorResponse
import mc.arch.pubapi.pigdi.dto.LeaderboardPage
import mc.arch.pubapi.pigdi.service.StatisticsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for leaderboard endpoints.
 *
 * @author Subham
 * @since 12/27/24
 */
@RestController
@RequestMapping("/v1/leaderboards")
@Tag(name = "Leaderboards", description = "Endpoints for accessing game leaderboards")
class LeaderboardsController(
    private val statisticsService: StatisticsService
)
{
    @GetMapping("/{statisticId}")
    @Operation(
        summary = "Get top players for a statistic",
        description = "Returns paginated leaderboard for any tracked statistic"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Leaderboard page"),
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
        @Parameter(description = "Full statistic ID")
        @PathVariable statisticId: String,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Results per page (max 100)")
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Any>
    {
        // Validate page and size
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

        val leaderboard = statisticsService.getLeaderboard(statisticId, page, clampedSize)
            ?: return ResponseEntity.status(404).body(
                ErrorResponse(
                    error = "STATISTIC_NOT_FOUND",
                    message = "No statistic found with ID '$statisticId'"
                )
            )

        return ResponseEntity.ok(leaderboard)
    }
}
