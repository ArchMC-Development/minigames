package mc.arch.pubapi.pigdi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import mc.arch.pubapi.pigdi.dto.BalTopMapResponse
import mc.arch.pubapi.pigdi.dto.BalTopResponse
import mc.arch.pubapi.pigdi.dto.EconomyProfileResponse
import mc.arch.pubapi.pigdi.dto.ErrorResponse
import mc.arch.pubapi.pigdi.service.EconomyService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * REST controller for economy endpoints.
 *
 * @author Subham
 * @since 12/28/24
 */
@RestController
@RequestMapping("/v1/economy")
@Tag(name = "Economy", description = "Endpoints for accessing player economy data (balances, profiles)")
class EconomyController(
    private val economyService: EconomyService
)
{
    @GetMapping("/baltop")
    @Operation(
        summary = "Get balance top leaderboard for all currencies",
        description = "Returns the top players by balance for all currency types (survival-coins, bedwars-experience, etc.)"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "BalTop leaderboard map keyed by currency type",
                content = [Content(schema = Schema(implementation = BalTopMapResponse::class))]
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
    fun getBalTop(): ResponseEntity<Map<String, BalTopResponse>>
    {
        return ResponseEntity.ok(economyService.getBalTop())
    }

    @GetMapping("/baltop/{currency}")
    @Operation(
        summary = "Get balance top leaderboard for a specific currency",
        description = "Returns the top players by balance for a specific currency type (e.g., survival-coins, bedwars-experience)"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "BalTop leaderboard for the specified currency",
                content = [Content(schema = Schema(implementation = BalTopResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Invalid or missing API key",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Currency not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "429",
                description = "Rate limit exceeded",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    fun getBalTopByCurrency(
        @Parameter(description = "Currency type (e.g., survival-coins, bedwars-experience)")
        @PathVariable currency: String
    ): ResponseEntity<Any>
    {
        val balTop = economyService.getBalTopByCurrency(currency)
            ?: return ResponseEntity.status(404).body(
                ErrorResponse(
                    error = "CURRENCY_NOT_FOUND",
                    message = "No baltop data found for currency '$currency'"
                )
            )

        return ResponseEntity.ok(balTop)
    }

    @GetMapping("/player/uuid/{uuid}")
    @Operation(
        summary = "Get economy profile by UUID",
        description = "Retrieves a player's economy profile including all balances and transaction statistics"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Player economy profile",
                content = [Content(schema = Schema(implementation = EconomyProfileResponse::class))]
            ),
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
    fun getEconomyProfileByUuid(
        @Parameter(description = "Minecraft UUID")
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

        val profile = economyService.getEconomyProfileByUuid(parsedUuid)
            ?: return ResponseEntity.status(404).body(
                ErrorResponse(
                    error = "PLAYER_NOT_FOUND",
                    message = "No economy profile found for UUID '$uuid'"
                )
            )

        return ResponseEntity.ok(profile)
    }

    @GetMapping("/player/username/{username}")
    @Operation(
        summary = "Get economy profile by username",
        description = "Retrieves a player's economy profile including all balances and transaction statistics"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Player economy profile",
                content = [Content(schema = Schema(implementation = EconomyProfileResponse::class))]
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
    fun getEconomyProfileByUsername(
        @Parameter(description = "Minecraft username")
        @PathVariable username: String
    ): ResponseEntity<Any>
    {
        val profile = economyService.getEconomyProfileByUsername(username)
            ?: return ResponseEntity.status(404).body(
                ErrorResponse(
                    error = "PLAYER_NOT_FOUND",
                    message = "No economy profile found for username '$username'"
                )
            )

        return ResponseEntity.ok(profile)
    }
}
