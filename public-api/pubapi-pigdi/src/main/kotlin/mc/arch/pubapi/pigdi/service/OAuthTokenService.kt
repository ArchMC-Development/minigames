package mc.arch.pubapi.pigdi.service

import com.google.gson.Gson
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Service for managing Discord OAuth tokens in Redis.
 * Used by the in-game plugin (via Redis) and the PIGDI API.
 *
 * @author Subham
 * @since 12/27/24
 */
@Service
class OAuthTokenService(
    private val redisTemplate: StringRedisTemplate,
    private val gson: Gson
)
{
    companion object
    {
        const val OAUTH_TOKEN_PREFIX = "akers:oauth:"
        const val OAUTH_TOKEN_TTL_SECONDS = 300L // 5 minutes
    }

    /**
     * Create and store a new OAuth token.
     * Called from the in-game plugin when player runs /api link.
     */
    fun createToken(minecraftUuid: UUID): OAuthToken
    {
        val token = OAuthToken(
            token = UUID.randomUUID().toString(),
            minecraftUuid = minecraftUuid.toString(),
            createdAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + (OAUTH_TOKEN_TTL_SECONDS * 1000)
        )

        val key = "$OAUTH_TOKEN_PREFIX${token.token}"
        val json = gson.toJson(token)

        redisTemplate.opsForValue().set(key, json, OAUTH_TOKEN_TTL_SECONDS, TimeUnit.SECONDS)

        return token
    }

    /**
     * Retrieve a token from Redis.
     */
    fun getToken(tokenId: String): OAuthToken?
    {
        val key = "$OAUTH_TOKEN_PREFIX$tokenId"
        val json = redisTemplate.opsForValue().get(key) ?: return null

        return try
        {
            val token = gson.fromJson(json, OAuthToken::class.java)
            if (token.isExpired())
            {
                deleteToken(tokenId)
                null
            }
            else
            {
                token
            }
        }
        catch (e: Exception)
        {
            null
        }
    }

    /**
     * Delete a token from Redis.
     */
    fun deleteToken(tokenId: String)
    {
        val key = "$OAUTH_TOKEN_PREFIX$tokenId"
        redisTemplate.delete(key)
    }

    /**
     * Build the OAuth initiation URL.
     */
    fun buildOAuthUrl(token: OAuthToken): String
    {
        return "https://api.arch.mc/auth/discord?token=${token.token}"
    }

    data class OAuthToken(
        val token: String,
        val minecraftUuid: String,
        val createdAt: Long,
        val expiresAt: Long
    )
    {
        fun isExpired() = System.currentTimeMillis() > expiresAt
        fun remainingMs() = (expiresAt - System.currentTimeMillis()).coerceAtLeast(0)
    }
}
