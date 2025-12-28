package mc.arch.pubapi.pigdi.service

import jakarta.annotation.PostConstruct
import mc.arch.pubapi.pigdi.dto.ClanLeaderboardEntry
import mc.arch.pubapi.pigdi.dto.ClanLeaderboardPage
import mc.arch.pubapi.pigdi.entity.ClanDocument
import mc.arch.pubapi.pigdi.repository.ClanRepository
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicReference

/**
 * Service for managing Clan leaderboards.
 *
 * Loads all clans from MongoDB into memory and refreshes every minute.
 * Clans are sorted by level (experience / 1000) in descending order.
 *
 * @author Subham
 * @since 12/28/24
 */
@Service
class ClanLeaderboardService(
    private val clanRepository: ClanRepository,
    private val redisTemplate: StringRedisTemplate
)
{
    private val logger = LoggerFactory.getLogger(ClanLeaderboardService::class.java)

    // In-memory cache of sorted clans
    private val sortedClans = AtomicReference<List<ClanLeaderboardEntry>>(emptyList())

    /**
     * Initialize clan leaderboard eagerly on startup.
     */
    @PostConstruct
    fun init()
    {
        logger.info("Initializing clan leaderboard service - performing eager MongoDB fetch...")
        refreshClanLeaderboard()
    }

    /**
     * Refresh the clan leaderboard every minute.
     */
    @Scheduled(fixedRate = 60000)
    fun refreshClanLeaderboard()
    {
        try
        {
            val clans = clanRepository.findAll()
            logger.debug("Fetched ${clans.size} clans from MongoDB")

            val leaderboard = clans
                .map { clan -> buildLeaderboardEntry(clan) }
                .sortedByDescending { it.level }
                .mapIndexed { index, entry -> entry.copy(position = index + 1) }

            sortedClans.set(leaderboard)
            logger.info("Refreshed clan leaderboard: ${leaderboard.size} clans loaded")
        }
        catch (e: Exception)
        {
            logger.error("Failed to refresh clan leaderboard", e)
        }
    }

    /**
     * Get paginated clan leaderboard.
     */
    fun getLeaderboard(page: Int, size: Int): ClanLeaderboardPage
    {
        val allClans = sortedClans.get()
        val totalClans = allClans.size
        val totalPages = if (totalClans == 0) 0 else (totalClans + size - 1) / size

        val start = page * size
        val end = minOf(start + size, totalClans)

        val entries = if (start < totalClans)
        {
            allClans.subList(start, end)
        }
        else
        {
            emptyList()
        }

        return ClanLeaderboardPage(
            page = page,
            size = size,
            totalPages = totalPages,
            totalClans = totalClans,
            entries = entries
        )
    }

    private fun buildLeaderboardEntry(clan: ClanDocument): ClanLeaderboardEntry
    {
        val leaderUsername = resolveUuidToUsername(clan.leader)

        return ClanLeaderboardEntry(
            position = 0, // Will be set after sorting
            uuid = clan.id,
            name = clan.name,
            displayName = clan.displayName,
            leaderUuid = clan.leader,
            leaderUsername = leaderUsername,
            level = clan.calculateLevel(),
            memberCount = clan.members.size
        )
    }

    private fun resolveUuidToUsername(uuid: String): String?
    {
        // Look up in Redis UUID cache (hash: DataStore:UuidCache:UUID)
        return redisTemplate.opsForHash<String, String>().get("DataStore:UuidCache:UUID", uuid)
    }
}
