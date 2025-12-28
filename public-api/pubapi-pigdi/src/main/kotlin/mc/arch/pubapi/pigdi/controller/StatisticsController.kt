package mc.arch.pubapi.pigdi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import mc.arch.pubapi.pigdi.dto.ErrorResponse
import mc.arch.pubapi.pigdi.dto.StatisticDetail
import mc.arch.pubapi.pigdi.dto.StatisticsListResponse
import mc.arch.pubapi.pigdi.service.StatisticsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for statistics endpoints.
 *
 * @author Subham
 * @since 12/27/24
 */
@RestController
@RequestMapping("/v1/statistics")
@Tag(name = "Statistics", description = "Endpoints for accessing statistic metadata")
class StatisticsController(
    private val statisticsService: StatisticsService
)
{
    @GetMapping
    @Operation(
        summary = "List all available statistic IDs",
        description = "Returns a complete list of all trackable statistic IDs in the system"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "List of all statistic IDs with metadata"
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
    fun listStatistics(): ResponseEntity<StatisticsListResponse>
    {
        val response = statisticsService.listAllStatistics()
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get detailed information about a specific statistic",
        description = "Returns metadata and schema for a single statistic ID"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Statistic details"
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
    fun getStatistic(
        @Parameter(description = "Full statistic ID (e.g., elo:nodebuff:ranked:lifetime)")
        @PathVariable id: String
    ): ResponseEntity<Any>
    {
        val detail = statisticsService.getStatisticDetail(id)
            ?: return ResponseEntity.status(404).body(
                ErrorResponse(
                    error = "STATISTIC_NOT_FOUND",
                    message = "No statistic found with ID '$id'"
                )
            )

        return ResponseEntity.ok(detail)
    }
}
