package mc.arch.minigames.persistent.housing.api.categorization

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import mc.arch.minigames.persistent.housing.api.categorization.model.CategorizationResult
import mc.arch.minigames.persistent.housing.api.categorization.provider.HttpHouseCategorizationProvider
import mc.arch.minigames.persistent.housing.api.categorization.provider.RedisStreamHouseCategorizationProvider
import mc.arch.minigames.persistent.housing.api.model.PlayerHouse
import mc.arch.minigames.persistent.housing.api.service.PlayerHousingService
import java.util.concurrent.CompletableFuture

/**
 * Entry point used by menus, commands, and save hooks. Owns provider
 * selection, short-circuits no-op re-runs via the content-hash cache, and
 * persists the result back onto the house.
 */
@Service
object HousingCategorizationService
{
    var config: CategorizationConfig = CategorizationConfig.DEFAULT
    var provider: HouseCategorizationProvider = pickDefault()

    @Configure
    fun configure()
    {
        provider = pickDefault()
    }

    fun categorize(house: PlayerHouse, force: Boolean = false): CompletableFuture<CategorizationResult>
    {
        val request = HouseTextExtractor.extract(house)

        val cached = house.derivedCategories
        if (!force && cached != null && cached.inputContentHash == request.contentHash)
        {
            return CompletableFuture.completedFuture(cached)
        }

        return provider.categorize(request).thenApply { result ->
            house.derivedCategories = result
            PlayerHousingService.save(house)
            result
        }
    }

    private fun pickDefault(): HouseCategorizationProvider
    {
        val transport = System.getenv("HOUSING_ML_TRANSPORT")?.lowercase() ?: "redis-stream"
        return when (transport)
        {
            "http" -> HttpHouseCategorizationProvider(config)
            else   -> RedisStreamHouseCategorizationProvider(config)
        }
    }
}
