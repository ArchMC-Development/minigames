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

        try
        {
            val defaults = DefaultHungerGamesKits.buildDefaultKits()
            val missing = defaults.filterKeys { it !in container.kits }

            if (missing.isEmpty())
            {
                return
            }

            logger.info("Found ${container.kits.size} existing kits, adding ${missing.size} missing default kits: ${missing.keys}")

            container.kits.putAll(missing)
            sync(container)
            logger.info("Successfully loaded missing default HG kits.")
        } catch (e: Exception)
        {
            logger.log(Level.SEVERE, "Failed to load default HG kits", e)
        }
    }

    override fun locatedIn() = DataSyncSource.Mongo
    override fun keys() = HGKitDataKeys
    override fun type() = HungerGamesKitContainer::class.java
}
