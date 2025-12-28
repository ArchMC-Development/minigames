package mc.arch.pubapi.akers.service

import gg.scala.commons.ScalaCommons
import mc.arch.pubapi.akers.model.DiscordOAuthToken
import net.evilblock.cubed.serializers.Serializers
import java.util.*

/**
 * Service for managing Discord OAuth tokens in Redis.
 *
 * @author Subham
 * @since 12/27/24
 */
object DiscordOAuthService
{
    private const val REDIS_PREFIX = "akers:oauth:"

    private fun redis() = ScalaCommons.bundle().globals().redis().sync()

    /**
     * Create and store a new OAuth token.
     */
    fun createToken(minecraftUuid: UUID): DiscordOAuthToken
    {
        val token = DiscordOAuthToken.create(minecraftUuid)
        storeToken(token)
        return token
    }

    /**
     * Store a token in Redis with TTL.
     */
    fun storeToken(token: DiscordOAuthToken)
    {
        val key = "$REDIS_PREFIX${token.token}"
        val json = Serializers.gson.toJson(token)
        val ttlSeconds = (token.remainingMs() / 1000).coerceAtLeast(1)

        redis().setex(key, ttlSeconds, json)
    }

    /**
     * Retrieve a token from Redis.
     */
    fun getToken(tokenId: UUID): DiscordOAuthToken?
    {
        val key = "$REDIS_PREFIX$tokenId"
        val json = redis().get(key) ?: return null

        return try
        {
            val token = Serializers.gson.fromJson(json, DiscordOAuthToken::class.java)
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
    fun deleteToken(tokenId: UUID)
    {
        val key = "$REDIS_PREFIX$tokenId"
        redis().del(key)
    }

    /**
     * Build the OAuth initiation URL.
     */
    fun buildOAuthUrl(token: DiscordOAuthToken): String
    {
        return "https://api.arch.mc/auth/discord?token=${token.token}"
    }
}
