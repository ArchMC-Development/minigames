package mc.arch.minigames.persistent.housing.api.service

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.store.controller.DataStoreObjectControllerCache
import mc.arch.minigames.persistent.housing.api.model.PlayerHouse

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
                "owner"
            )
    }
}