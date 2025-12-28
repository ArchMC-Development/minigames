package mc.arch.pubapi.pigdi.service

import mc.arch.pubapi.pigdi.entity.AkersProfileDocument
import mc.arch.pubapi.pigdi.repository.AkersProfileRepository
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

/**
 * Service for managing AKERS profiles and API key validation.
 *
 * @author Subham
 * @since 12/27/24
 */
@Service
class AkersService(
    private val akersProfileRepository: AkersProfileRepository
)
{
    // Local cache for API key -> Profile ID mapping for fast lookups
    private val apiKeyCache = ConcurrentHashMap<String, String>()

    /**
     * Find a profile by API key token.
     * Returns null if key is invalid, revoked, or owner is banned.
     */
    fun findProfileByApiKey(token: String): AkersProfileDocument?
    {
        // Check cache first
        val cachedProfileId = apiKeyCache[token]
        if (cachedProfileId != null)
        {
            val profile = akersProfileRepository.findById(cachedProfileId).orElse(null)
            if (profile != null)
            {
                val key = profile.findKeyByToken(token)
                if (key != null && !profile.banned)
                {
                    return profile
                }
            }
            // Invalid cache entry
            apiKeyCache.remove(token)
        }

        // Search all profiles (expensive, but necessary for uncached keys)
        val allProfiles = akersProfileRepository.findAll()
        for (profile in allProfiles)
        {
            val key = profile.findKeyByToken(token)
            if (key != null)
            {
                if (!profile.banned)
                {
                    // Cache for future lookups
                    apiKeyCache[token] = profile.id
                    return profile
                }
                return null // Found but banned
            }
        }

        return null
    }

    /**
     * Validate an API key and return owner info.
     */
    fun validateApiKey(token: String): ApiKeyValidation
    {
        val profile = findProfileByApiKey(token)
            ?: return ApiKeyValidation(valid = false, reason = "Invalid or revoked API key")

        if (profile.banned)
        {
            return ApiKeyValidation(
                valid = false,
                reason = "Account banned: ${profile.banReason ?: "No reason provided"}"
            )
        }

        return ApiKeyValidation(
            valid = true,
            ownerId = profile.id,
            reason = null
        )
    }

    /**
     * Update last used timestamp for an API key.
     */
    fun updateKeyLastUsed(token: String)
    {
        val profileId = apiKeyCache[token] ?: return
        val profile = akersProfileRepository.findById(profileId).orElse(null) ?: return

        val key = profile.apiKeys.find { it.token == token }
        if (key != null)
        {
            key.lastUsedAt = System.currentTimeMillis().toString()
            akersProfileRepository.save(profile)
        }
    }

    /**
     * Invalidate cache entry for a token.
     */
    fun invalidateCache(token: String)
    {
        apiKeyCache.remove(token)
    }

    data class ApiKeyValidation(
        val valid: Boolean,
        val ownerId: String? = null,
        val reason: String? = null
    )
}
