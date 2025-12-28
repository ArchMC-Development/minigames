package mc.arch.pubapi.pigdi.model

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Enum representing UGC (User Generated Content) gamemodes.
 *
 * Maps user-facing gamemode names to their internal Redis key prefixes.
 *
 * @author Subham
 * @since 12/28/24
 */
@Schema(
    description = "UGC Gamemodes available for statistics queries",
    enumAsRef = true
)
enum class UGCGamemode(
    val redisPrefix: String,
    val displayName: String,
    val description: String
)
{
    @Schema(description = "Lifesteal gamemode - PvP with health stealing mechanics")
    TROJAN("trojan", "Lifesteal", "Lifesteal gamemode - PvP with health stealing mechanics"),

    @Schema(description = "Survival gamemode - Classic survival experience")
    SPARTAN("rootkit", "Survival", "Survival gamemode - Classic survival experience");

    companion object
    {
        /**
         * Find a gamemode by its API name (case-insensitive).
         */
        fun fromApiName(name: String): UGCGamemode?
        {
            return entries.find { it.name.equals(name, ignoreCase = true) }
        }
    }
}
