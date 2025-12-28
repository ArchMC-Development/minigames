package mc.arch.pubapi.pigdi.service

import jakarta.annotation.PostConstruct
import mc.arch.pubapi.pigdi.dto.PlaytimeLeaderboardEntry
import mc.arch.pubapi.pigdi.dto.PlaytimeLeaderboardPage
import mc.arch.pubapi.pigdi.model.UGCGamemode
import mc.arch.pubapi.pigdi.repository.LifestealProfileRepository
import mc.arch.pubapi.pigdi.repository.SurvivalProfileRepository
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicReference

/**
 * Service for managing UGC playtime leaderboards.
 *
 * Loads top 1000 players by playtime from MongoDB into memory and refreshes every minute.
 *
 * @author Subham
 * @since 12/28/24
 */
@Service
class UGCPlaytimeLeaderboardService(
    private val lifestealProfileRepository: LifestealProfileRepository,
    private val survivalProfileRepository: SurvivalProfileRepository,
    private val redisTemplate: StringRedisTemplate
)
{
    private val logger = LoggerFactory.getLogger(UGCPlaytimeLeaderboardService::class.java)

    // In-memory cache of sorted playtime entries per gamemode
    private val trojanLeaderboard = AtomicReference<List<PlaytimeLeaderboardEntry>>(emptyList())
    private val spartanLeaderboard = AtomicReference<List<PlaytimeLeaderboardEntry>>(emptyList())

    /**
     * Initialize playtime leaderboards eagerly on startup.
     */
    @PostConstruct
    fun init()
    {
        logger.info("Initializing UGC playtime leaderboard service - performing eager MongoDB fetch...")
        refreshLeaderboards()
    }

    /**
     * Refresh the playtime leaderboards every minute.
     */
    @Scheduled(fixedRate = 60000)
    fun refreshLeaderboards()
    {
        try
        {
            refreshTrojanLeaderboard()
            refreshSpartanLeaderboard()
        }
        catch (e: Exception)
        {
            logger.error("Failed to refresh playtime leaderboards", e)
        }
    }

    /**
     * Get paginated playtime leaderboard for a gamemode.
     */
    fun getLeaderboard(gamemode: UGCGamemode, page: Int, size: Int): PlaytimeLeaderboardPage
    {
        val allEntries = when (gamemode)
        {
            UGCGamemode.TROJAN -> trojanLeaderboard.get()
            UGCGamemode.SPARTAN -> spartanLeaderboard.get()
        }

        val totalPlayers = allEntries.size
        val totalPages = if (totalPlayers == 0) 0 else (totalPlayers + size - 1) / size

        val start = page * size
        val end = minOf(start + size, totalPlayers)

        val entries = if (start < totalPlayers)
        {
            allEntries.subList(start, end)
        }
        else
        {
            emptyList()
        }

        return PlaytimeLeaderboardPage(
            gamemode = gamemode.name.lowercase(),
            gamemodeDisplayName = gamemode.displayName,
            page = page,
            size = size,
            totalPages = totalPages,
            totalPlayers = totalPlayers,
            entries = entries
        )
    }

    // ==================== Private Methods ====================

    private fun refreshTrojanLeaderboard()
    {
        val profiles = lifestealProfileRepository.findAll()
        logger.debug("Fetched ${profiles.size} Lifesteal profiles from MongoDB")

        val leaderboard = profiles
            .map { profile ->
                PlaytimeLeaderboardEntry(
                    position = 0, // Set after sorting
                    uuid = profile.identifier,
                    username = resolveUuidToUsername(profile.identifier),
                    playtimeSeconds = profile.getPlaytimeSeconds()
                )
            }
            .sortedByDescending { it.playtimeSeconds }
            .take(1000)
            .mapIndexed { index, entry -> entry.copy(position = index + 1) }

        trojanLeaderboard.set(leaderboard)
        logger.info("Refreshed Trojan playtime leaderboard: ${leaderboard.size} players loaded")
    }

    private fun refreshSpartanLeaderboard()
    {
        val profiles = survivalProfileRepository.findAll()
        logger.debug("Fetched ${profiles.size} Survival profiles from MongoDB")

        val leaderboard = profiles
            .map { profile ->
                PlaytimeLeaderboardEntry(
                    position = 0, // Set after sorting
                    uuid = profile.identifier,
                    username = resolveUuidToUsername(profile.identifier),
                    playtimeSeconds = profile.getPlaytimeSeconds()
                )
            }
            .sortedByDescending { it.playtimeSeconds }
            .take(1000)
            .mapIndexed { index, entry -> entry.copy(position = index + 1) }

        spartanLeaderboard.set(leaderboard)
        logger.info("Refreshed Spartan playtime leaderboard: ${leaderboard.size} players loaded")
    }

    private fun resolveUuidToUsername(uuid: String): String?
    {
        return redisTemplate.opsForHash<String, String>().get("DataStore:UuidCache:UUID", uuid)
    }
}
