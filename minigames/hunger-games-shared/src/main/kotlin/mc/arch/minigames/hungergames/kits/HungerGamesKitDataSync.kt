package mc.arch.minigames.hungergames.kits

import gg.scala.commons.persist.datasync.DataSyncKeys
import gg.scala.commons.persist.datasync.DataSyncService
import gg.scala.commons.persist.datasync.DataSyncSource
import gg.scala.flavor.service.Service

/**
 * @author ArchMC
 */
@Service
object HungerGamesKitDataSync : DataSyncService<HungerGamesKitContainer>()
{
    data object HGKitDataKeys : DataSyncKeys
    {
        override fun newStore() = "hungergames-kits"
        override fun store() = keyOf("hgkits", "store")
        override fun sync() = keyOf("hgkits", "sync")
    }

    override fun locatedIn() = DataSyncSource.Mongo
    override fun keys() = HGKitDataKeys
    override fun type() = HungerGamesKitContainer::class.java
}
