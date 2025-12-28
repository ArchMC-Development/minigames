package mc.arch.pubapi.pigdi.model

import java.util.*

/**
 * API key data model for AKERS.
 * Token format: amc-akers_{12-char-base64}
 *
 * @author Subham
 * @since 12/27/24
 */
data class AkersApiKey(
    val token: String,
    var name: String,
    val createdAt: String = System.currentTimeMillis().toString(),
    var lastUsedAt: String? = null,
    var revoked: Boolean = false,
    var revokedAt: String? = null,
    var revokedBy: UUID? = null
)
{
    companion object
    {
        const val TOKEN_PREFIX = "amc-akers_"

        fun isValidTokenFormat(token: String): Boolean
        {
            if (!token.startsWith(TOKEN_PREFIX)) return false
            val suffix = token.removePrefix(TOKEN_PREFIX)
            return suffix.length == 12 && suffix.all { it.isLetterOrDigit() || it == '-' || it == '_' }
        }
    }

    fun markUsed()
    {
        lastUsedAt = System.currentTimeMillis().toString()
    }

    fun isActive() = !revoked
}
