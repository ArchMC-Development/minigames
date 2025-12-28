package mc.arch.pubapi.pigdi.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * OpenAPI/Swagger configuration for PIGDI.
 *
 * @author Subham
 * @since 12/27/24
 */
@Configuration
class OpenApiConfig
{
    @Bean
    fun customOpenAPI(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("PIGDI - Public Immutable Game Data Interface")
                .version("1.0.0")
                .description(
                    """
                    Public API for accessing Arch MC game statistics.
                    
                    ## Authentication
                    All endpoints require the `X-API-KEY` header with a valid API key.
                    API keys can be obtained through the in-game `/api` command after linking your Discord account.
                    
                    ## Rate Limiting
                    - 100 requests per minute per API key
                    - Rate limit headers are included in all responses
                    
                    ## Statistic ID Format
                    Statistics follow the format: `{type}:{kit}:{queueType}:{lifetime}`
                    
                    Examples:
                    - `elo:nodebuff:ranked:lifetime` - Ranked ELO for NoDebuff kit
                    - `wins:global:casual:daily` - Daily casual wins across all kits
                    """.trimIndent()
                )
                .contact(
                    Contact()
                        .name("Arch MC")
                        .url("https://arch.mc")
                )
        )
        .addSecurityItem(SecurityRequirement().addList("ApiKeyAuth"))
        .components(
            Components()
                .addSecuritySchemes(
                    "ApiKeyAuth",
                    SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .`in`(SecurityScheme.In.HEADER)
                        .name("X-API-KEY")
                        .description("API key obtained from in-game /api command")
                )
        )
}
