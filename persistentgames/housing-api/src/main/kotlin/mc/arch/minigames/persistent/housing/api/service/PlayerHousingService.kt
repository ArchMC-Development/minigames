package mc.arch.minigames.persistent.housing.api.service

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import mc.arch.minigames.persistent.housing.api.model.PlayerHouse
import java.util.UUID
import kotlin.uuid.Uuid

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

    fun findById(uuid: UUID) = controller.load(uuid, DataStoreStorageType.MONGO)
}