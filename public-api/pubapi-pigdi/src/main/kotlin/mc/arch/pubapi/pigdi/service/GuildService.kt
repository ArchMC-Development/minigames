package mc.arch.pubapi.pigdi.service

import jakarta.annotation.PostConstruct
import mc.arch.pubapi.pigdi.dto.GuildMemberResponse
import mc.arch.pubapi.pigdi.dto.GuildResponse
import mc.arch.pubapi.pigdi.entity.GuildDocument
import mc.arch.pubapi.pigdi.repository.GuildRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.atomic.AtomicReference

/**
 * Service for managing Guild data with 1-minute cache refresh.
 *
 * @author Subham
 * @since 12/28/24
 */
@Service
class GuildService(
    private val guildRepository: GuildRepository,
    private val uuidCacheService: UuidCacheService
)
{
    private val logger = LoggerFactory.getLogger(GuildService::class.java)

    // In-memory cache of all guilds
    private val guildsCache = AtomicReference<List<GuildDocument>>(emptyList())

    // Index by player UUID for fast lookups
    private val playerGuildIndex = AtomicReference<Map<String, GuildDocument>>(emptyMap())

    /**
     * Initialize guild cache eagerly on startup.
     */
    @PostConstruct
    fun init()
    {
        logger.info("Initializing guild service - performing eager MongoDB fetch...")
        refreshGuildCache()
    }

    /**
     * Refresh the guild cache every minute.
     */
    @Scheduled(fixedRate = 60000)
    fun refreshGuildCache()
    {
        try
        {
            val guilds = guildRepository.findAll()
            guildsCache.set(guilds)

            // Build player -> guild index
            val playerIndex = mutableMapOf<String, GuildDocument>()
            for (guild in guilds)
            {
                // Index creator
                guild.creator?.uniqueId?.let { playerIndex[it] = guild }

                // Index members
                guild.members?.forEach { (uuid, _) ->
                    playerIndex[uuid] = guild
                }
            }
            playerGuildIndex.set(playerIndex)

            logger.info("Refreshed guild cache: ${guilds.size} guilds loaded, ${playerIndex.size} player mappings")
        }
        catch (e: Exception)
        {
            logger.error("Failed to refresh guild cache", e)
        }
    }

    /**
     * Get guild by identifier (UUID).
     */
    fun getGuildById(id: String): GuildResponse?
    {
        val guild = guildsCache.get().find { it.identifier == id } ?: return null
        return toGuildResponse(guild)
    }

    /**
     * Get guild by player UUID.
     */
    fun getGuildByPlayerUuid(uuid: UUID): GuildResponse?
    {
        val guild = playerGuildIndex.get()[uuid.toString()] ?: return null
        return toGuildResponse(guild)
    }

    /**
     * Get guild by player username.
     */
    fun getGuildByPlayerUsername(username: String): GuildResponse?
    {
        val uuid = uuidCacheService.resolveUsernameToUuid(username) ?: return null
        return getGuildByPlayerUuid(uuid)
    }

    /**
     * Search guilds by name (case-insensitive, contains).
     */
    fun searchGuildsByName(query: String): List<GuildResponse>
    {
        return guildsCache.get()
            .filter { it.name.contains(query, ignoreCase = true) }
            .take(50)
            .map { toGuildResponse(it) }
    }

    /**
     * Search guilds by description (case-insensitive, contains).
     */
    fun searchGuildsByDescription(query: String): List<GuildResponse>
    {
        return guildsCache.get()
            .filter { it.description?.contains(query, ignoreCase = true) == true }
            .take(50)
            .map { toGuildResponse(it) }
    }

    /**
     * Get all guilds (paginated).
     */
    fun getAllGuilds(page: Int, size: Int): List<GuildResponse>
    {
        val allGuilds = guildsCache.get()
        val start = page * size
        val end = minOf(start + size, allGuilds.size)

        return if (start < allGuilds.size)
        {
            allGuilds.subList(start, end).map { toGuildResponse(it) }
        }
        else
        {
            emptyList()
        }
    }

    /**
     * Get total guild count.
     */
    fun getTotalGuildCount(): Int = guildsCache.get().size

    // ==================== Private Methods ====================

    private fun toGuildResponse(guild: GuildDocument): GuildResponse
    {
        val creatorResponse = guild.creator?.let { creator ->
            GuildMemberResponse(
                uuid = creator.uniqueId,
                username = uuidCacheService.resolveUuidToUsername(creator.uniqueId),
                joinedOn = creator.joinedOn,
                role = creator.role ?: "Leader"
            )
        }

        val memberResponses = guild.members?.map { (uuid, member) ->
            GuildMemberResponse(
                uuid = uuid,
                username = uuidCacheService.resolveUuidToUsername(uuid),
                joinedOn = member.joinedOn,
                role = member.role ?: "Member"
            )
        } ?: emptyList()

        return GuildResponse(
            id = guild.identifier,
            name = guild.name,
            description = guild.description,
            createdOn = guild.createdOn,
            creator = creatorResponse,
            members = memberResponses,
            memberCount = memberResponses.size + (if (creatorResponse != null) 1 else 0),
            allInvite = guild.allInvite
        )
    }
}

