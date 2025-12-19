package mc.arch.minigames.persistent.housing.api.service

import com.mongodb.client.model.Filters
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import mc.arch.minigames.persistent.housing.api.model.PlayerHouse
import mc.arch.minigames.persistent.housing.api.cache.MutableHousingCache
import java.util.*

@Service
object PlayerHousingService
{
    private val controller get() = DataStoreObjectControllerCache.findNotNull<PlayerHouse>()

    @Configure
    fun configure()
    {
        DataStoreObjectControllerCache.create<PlayerHouse>()

        controller.mongo()
            .createIndexesFor(
                "owner",
                "name"
            )
    }

    fun save(house: PlayerHouse) =
        controller.save(house, DataStoreStorageType.MONGO).thenAccept {
            MutableHousingCache.cache(house)
        }

    fun findByName(name : String) = controller.mongo().loadWithFilter(Filters.eq("name", name.lowercase()))

    fun findById(uuid: UUID) = controller.load(uuid, DataStoreStorageType.MONGO)
}