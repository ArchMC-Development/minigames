package mc.arch.pubapi.akers.model

import gg.scala.commons.annotations.Model
import java.util.*

/**
 * Temporary OAuth token for Discord linking flow.
 * Plain data class with no framework dependencies.
 *
 * @author Subham
 * @since 12/27/24
 */
data class DiscordOAuthToken(
    val token: UUID,
    val minecraftUuid: UUID,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + EXPIRY_MS
)
{
    companion object
    {
        const val EXPIRY_MS = 300_000L // 5 minutes

        fun create(minecraftUuid: UUID) = DiscordOAuthToken(
            token = UUID.randomUUID(),
            minecraftUuid = minecraftUuid
        )
    }

    fun isExpired() = System.currentTimeMillis() > expiresAt

    fun remainingMs() = (expiresAt - System.currentTimeMillis()).coerceAtLeast(0)
}
