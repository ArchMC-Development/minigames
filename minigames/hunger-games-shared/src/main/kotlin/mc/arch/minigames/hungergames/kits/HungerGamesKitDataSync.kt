package mc.arch.minigames.hungergames.kits

import gg.scala.commons.persist.datasync.DataSyncKeys
import gg.scala.commons.persist.datasync.DataSyncService
import gg.scala.commons.persist.datasync.DataSyncSource
import gg.scala.flavor.service.Service
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author ArchMC
 */
@Service
object HungerGamesKitDataSync : DataSyncService<HungerGamesKitContainer>()
{
    private val logger = Logger.getLogger("HGKitDataSync")

    data object HGKitDataKeys : DataSyncKeys
    {
        override fun newStore() = "hungergames-kits"
        override fun store() = keyOf("hgkits", "store")
        override fun sync() = keyOf("hgkits", "sync")
    }

    override fun postReload()
    {
        val container = cached()
        if (container.kits.isEmpty())
        {
            logger.info("No kits found in DataSync, populating with ${DefaultHungerGamesKits.buildDefaultKits().size} default kits...")

            try
            {
                container.kits.putAll(DefaultHungerGamesKits.buildDefaultKits())
                sync(container)
                logger.info("Successfully loaded default HG kits.")
            } catch (e: Exception)
            {
                logger.log(Level.SEVERE, "Failed to load default HG kits", e)
            }
        }
    }

    override fun locatedIn() = DataSyncSource.Mongo
    override fun keys() = HGKitDataKeys
    override fun type() = HungerGamesKitContainer::class.java
}
