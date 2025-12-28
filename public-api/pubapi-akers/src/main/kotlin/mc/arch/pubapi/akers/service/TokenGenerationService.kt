package mc.arch.pubapi.akers.service

import mc.arch.pubapi.akers.model.AkersApiKey
import java.security.SecureRandom
import java.util.*

/**
 * Service for generating cryptographically secure tokens.
 *
 * @author Subham
 * @since 12/27/24
 */
object TokenGenerationService
{
    private val secureRandom = SecureRandom()

    /**
     * Generate a new API key in format: amc-akers_{12-char-base64}
     */
    fun generateApiKey(): String
    {
        val bytes = ByteArray(9) // 9 bytes = 12 base64 chars
        secureRandom.nextBytes(bytes)
        val encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        return "${AkersApiKey.TOKEN_PREFIX}$encoded"
    }

    /**
     * Generate a new OAuth token UUID.
     */
    fun generateOAuthToken(): UUID = UUID.randomUUID()

    /**
     * Create a new API key with the given name.
     */
    fun createApiKey(name: String): AkersApiKey
    {
        return AkersApiKey(
            token = generateApiKey(),
            name = name
        )
    }
}
