package mc.arch.pubapi.pigdi.service

import mc.arch.pubapi.pigdi.dto.ItemFilterResponse
import mc.arch.pubapi.pigdi.dto.LifestealProfileResponse
import mc.arch.pubapi.pigdi.repository.LifestealProfileRepository
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.*

/**
 * Service for fetching UGC player profiles.
 *
 * @author Subham
 * @since 12/28/24
 */
@Service
class UGCProfileService(
    private val lifestealProfileRepository: LifestealProfileRepository,
    private val redisTemplate: StringRedisTemplate
)
{
    private val logger = LoggerFactory.getLogger(UGCProfileService::class.java)

    /**
     * Get a Lifesteal profile by UUID.
     */
    fun getLifestealProfile(uuid: UUID): LifestealProfileResponse?
    {
        val profile = lifestealProfileRepository.findByIdentifier(uuid.toString())
            ?: return null

        val username = resolveUuidToUsername(uuid.toString())

        return LifestealProfileResponse(
            uuid = profile.identifier,
            username = username,
            hearts = profile.getHearts(),
            totalPlaytimeSeconds = profile.getPlaytimeSeconds(),
            renameTokens = profile.renameTokens,
            homeCount = profile.getHomeCount(),
            publicHomeCount = profile.getPublicHomeCount(),
            itemFilter = profile.itemFilter?.let { filter ->
                ItemFilterResponse(
                    enabled = filter.enabled,
                    type = filter.type,
                    excludedBlockCount = filter.getExcludedBlockCount(),
                    excludedPotionCount = filter.getExcludedPotionCount()
                )
            },
            ignoredTips = profile.ignoredTips ?: emptyList(),
            hasMigratedV1Homes = profile.hasMigratedV1Homes,
            hasMigratedV2Homes = profile.hasMigratedV2Homes
        )
    }

    /**
     * Get a Lifesteal profile by username.
     */
    fun getLifestealProfileByUsername(username: String): LifestealProfileResponse?
    {
        val uuid = resolveUsername(username) ?: return null
        return getLifestealProfile(uuid)
    }

    private fun resolveUsername(username: String): UUID?
    {
        val uuid = redisTemplate.opsForHash<String, String>().get("DataStore:UuidCache:Username", username)
        return uuid?.let {
            try { UUID.fromString(it) } catch (e: Exception) { null }
        }
    }

    private fun resolveUuidToUsername(uuid: String): String?
    {
        return redisTemplate.opsForHash<String, String>().get("DataStore:UuidCache:UUID", uuid)
    }
}
