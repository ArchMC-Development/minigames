package mc.arch.pubapi.pigdi.service

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import mc.arch.pubapi.pigdi.dto.*
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.*

/**
 * Service for economy-related data (balances, profiles, baltop).
 *
 * @author Subham
 * @since 12/28/24
 */
@Service
class EconomyService(
    private val redisTemplate: StringRedisTemplate,
    private val mongoTemplate: MongoTemplate,
    private val gson: Gson
)
{
    private val logger = LoggerFactory.getLogger(EconomyService::class.java)

    companion object
    {
        const val BALTOP_REDIS_KEY = "economy:global:top"
        const val UUID_CACHE_KEY = "DataStore:UuidCache:UUID"
        const val USERNAME_CACHE_KEY = "DataStore:UuidCache:Username"
        const val ECONOMY_COLLECTION = "EconomyProfile"
    }

    /**
     * Get the balance top leaderboard from Redis.
     */
    fun getBalTop(): BalTopResponse?
    {
        return try
        {
            val rawData = redisTemplate.opsForValue().get(BALTOP_REDIS_KEY)
            if (rawData == null)
            {
                logger.warn("BalTop data not found in Redis at key: $BALTOP_REDIS_KEY")
                return null
            }

            val jsonObject = gson.fromJson(rawData, JsonObject::class.java)
            val playersArray = jsonObject.getAsJsonArray("players")

            val entries = playersArray.mapIndexed { index, element ->
                val player = element.asJsonObject
                val uuid = player.get("_id").asString
                val balance = player.get("balance").asLong

                BalTopEntry(
                    position = index + 1,
                    uuid = uuid,
                    username = resolveUuidToUsername(uuid) ?: uuid,
                    balance = balance
                )
            }

            BalTopResponse(
                entries = entries,
                count = entries.size
            )
        }
        catch (e: Exception)
        {
            logger.error("Failed to parse BalTop data from Redis", e)
            null
        }
    }

    /**
     * Get economy profile by UUID.
     */
    fun getEconomyProfileByUuid(uuid: UUID): EconomyProfileResponse?
    {
        return try
        {
            val query = Query.query(Criteria.where("_id").`is`(uuid.toString()))
            val document = mongoTemplate.findOne(query, org.bson.Document::class.java, ECONOMY_COLLECTION)
                ?: return null

            parseEconomyProfile(document)
        }
        catch (e: Exception)
        {
            logger.error("Failed to fetch economy profile for UUID: $uuid", e)
            null
        }
    }

    /**
     * Get economy profile by username.
     */
    fun getEconomyProfileByUsername(username: String): EconomyProfileResponse?
    {
        val uuid = resolveUsernameToUuid(username) ?: return null
        return getEconomyProfileByUuid(uuid)
    }

    /**
     * Parse a MongoDB document into an EconomyProfileResponse.
     */
    private fun parseEconomyProfile(document: org.bson.Document): EconomyProfileResponse
    {
        val uuid = document.getString("_id") ?: document.getString("identifier")

        // Parse balances
        val balancesDoc = document.get("balances", org.bson.Document::class.java) ?: org.bson.Document()
        val balances = mutableMapOf<String, Long>()
        balancesDoc.forEach { key, value ->
            balances[key] = when (value)
            {
                is Number -> value.toLong()
                else -> 0L
            }
        }

        // Parse statistics
        val statisticsDoc = document.get("statistics", org.bson.Document::class.java) ?: org.bson.Document()
        val statistics = mutableMapOf<String, CurrencyStatistics>()
        statisticsDoc.forEach { currency, statsValue ->
            if (statsValue is org.bson.Document)
            {
                statistics[currency] = CurrencyStatistics(
                    spent = statsValue.getNumber("spent")?.toLong() ?: 0L,
                    made = statsValue.getNumber("made")?.toLong() ?: 0L,
                    wagered = statsValue.getNumber("wagered")?.toLong() ?: 0L
                )
            }
        }

        return EconomyProfileResponse(
            uuid = uuid,
            username = resolveUuidToUsername(uuid) ?: uuid,
            balances = balances,
            statistics = statistics
        )
    }

    /**
     * Resolve UUID to username using Redis cache.
     */
    private fun resolveUuidToUsername(uuid: String): String?
    {
        return try
        {
            redisTemplate.opsForHash<String, String>().get(UUID_CACHE_KEY, uuid)
        }
        catch (e: Exception)
        {
            logger.warn("Failed to resolve UUID to username: $uuid", e)
            null
        }
    }

    /**
     * Resolve username to UUID using Redis cache.
     */
    private fun resolveUsernameToUuid(username: String): UUID?
    {
        return try
        {
            val uuid = redisTemplate.opsForHash<String, String>().get(USERNAME_CACHE_KEY, username.lowercase())
            uuid?.let { UUID.fromString(it) }
        }
        catch (e: Exception)
        {
            logger.warn("Failed to resolve username to UUID: $username", e)
            null
        }
    }

    // Helper extension
    private fun org.bson.Document.getNumber(key: String): Number?
    {
        return this[key] as? Number
    }
}
