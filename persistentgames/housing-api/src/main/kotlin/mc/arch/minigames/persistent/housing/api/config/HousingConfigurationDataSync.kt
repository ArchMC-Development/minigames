package mc.arch.minigames.persistent.housing.api.config

import gg.scala.commons.persist.datasync.DataSyncKeys
import gg.scala.commons.persist.datasync.DataSyncService
import gg.scala.commons.persist.datasync.DataSyncSource
import gg.scala.flavor.service.Service

/**
 * DataSync service for global housing configuration.
 *
 * @author ArchMC
 */
@Service
object HousingConfigurationDataSync : DataSyncService<HousingConfiguration>()
{
    data object HousingConfigKeys : DataSyncKeys
    {
        override fun newStore() = "housing-configuration"
        override fun store() = keyOf("housing-config", "store")
        override fun sync() = keyOf("housing-config", "sync")
    }

    override fun locatedIn() = DataSyncSource.Mongo
    override fun keys() = HousingConfigKeys
    override fun type() = HousingConfiguration::class.java
}
