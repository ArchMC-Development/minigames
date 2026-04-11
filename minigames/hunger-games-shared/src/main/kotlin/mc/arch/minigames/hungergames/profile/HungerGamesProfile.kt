package mc.arch.minigames.hungergames.profile

import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.storable.IDataStoreObject
import gg.scala.store.storage.type.DataStoreStorageType
import java.util.*

/**
 * @author ArchMC
 */
data class HungerGamesProfile(
    override val identifier: UUID,
    var selectedKit: String? = null,
    var selectedKitLevel: Int = 1
) : IDataStoreObject
{
    fun save() = DataStoreObjectControllerCache
        .findNotNull<HungerGamesProfile>()
        .save(this, DataStoreStorageType.MONGO)
}
