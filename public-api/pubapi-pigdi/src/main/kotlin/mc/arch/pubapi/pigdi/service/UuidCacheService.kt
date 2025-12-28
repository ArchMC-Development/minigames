package mc.arch.pubapi.pigdi.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.*

/**
 * Service for UUID <-> Username cache operations.
 *
 * Abstracts Redis hash lookups for player UUID and username resolution.
 * Uses the DataStore:UuidCache:UUID and DataStore:UuidCache:Username hashes.
 *
 * @author Subham
 * @since 12/28/24
 */
@Service
class UuidCacheService(
    private val redisTemplate: StringRedisTemplate
)
{
    companion object
    {
        private const val UUID_TO_USERNAME_KEY = "DataStore:UuidCache:UUID"
        private const val USERNAME_TO_UUID_KEY = "DataStore:UuidCache:Username"
    }

    /**
     * Resolve a UUID to a username.
     *
     * @param uuid The player UUID
     * @return The username, or null if not found
     */
    fun resolveUuidToUsername(uuid: UUID): String?
    {
        return resolveUuidToUsername(uuid.toString())
    }

    /**
     * Resolve a UUID string to a username.
     *
     * @param uuid The player UUID as string
     * @return The username, or null if not found
     */
    fun resolveUuidToUsername(uuid: String): String?
    {
        return redisTemplate.opsForHash<String, String>().get(UUID_TO_USERNAME_KEY, uuid)
    }

    /**
     * Resolve a username to a UUID.
     *
     * @param username The player username
     * @return The UUID, or null if not found
     */
    fun resolveUsernameToUuid(username: String): UUID?
    {
        val uuidString = redisTemplate.opsForHash<String, String>().get(USERNAME_TO_UUID_KEY, username.lowercase())
        return uuidString?.let {
            try
            {
                UUID.fromString(it)
            }
            catch (e: Exception)
            {
                null
            }
        }
    }

    /**
     * Resolve a username to a UUID string.
     *
     * @param username The player username
     * @return The UUID as string, or null if not found
     */
    fun resolveUsernameToUuidString(username: String): String?
    {
        return redisTemplate.opsForHash<String, String>().get(USERNAME_TO_UUID_KEY, username.lowercase())
    }

    /**
     * Batch resolve multiple UUIDs to usernames.
     *
     * @param uuids List of UUIDs to resolve
     * @return Map of UUID string to username (only contains entries for UUIDs that were found)
     */
    fun batchResolveUuidsToUsernames(uuids: List<String>): Map<String, String>
    {
        if (uuids.isEmpty()) return emptyMap()

        val hashOps = redisTemplate.opsForHash<String, String>()
        val results = hashOps.multiGet(UUID_TO_USERNAME_KEY, uuids)

        return uuids.zip(results)
            .filter { (_, username) -> username != null }
            .associate { (uuid, username) -> uuid to username!! }
    }
}
