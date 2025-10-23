package gg.tropic.practice.map

import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.persist.datasync.DataSyncKeys
import gg.scala.commons.persist.datasync.DataSyncService
import gg.scala.commons.persist.datasync.DataSyncSource
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Service
import gg.tropic.practice.PracticeShared
import gg.tropic.practice.namespace
import gg.tropic.practice.namespaceShortened
import gg.tropic.practice.suffixWhenDev
import net.kyori.adventure.key.Key
import java.util.logging.Logger

/**
 * @author GrowlyX
 * @since 9/21/2023
 */
@Service
object MapService : DataSyncService<MapContainer>()
{
    @Inject
    lateinit var plugin: ExtendedScalaPlugin

    object MapKeys : DataSyncKeys
    {
        override fun newStore() = "mi-practice-maps"

        override fun store() = Key.key(namespace(), "maps")
        override fun sync() = Key.key(namespaceShortened().suffixWhenDev(), "msync")
    }

    override fun locatedIn() = DataSyncSource.Mongo

    override fun keys() = MapKeys
    override fun type() = MapContainer::class.java

    var fastMapIndex = mapOf<String, Map>()
    fun rebuildFastMapIndex()
    {
        fastMapIndex = cached().maps.values.associateBy { it.name }
        Logger.getGlobal().info("[fast-index] Rebuilt fast map indexes.")
    }

    fun maps() = cached().maps.values

    fun mapByAbsoluteID(absolute: String) = fastMapIndex[absolute]
    fun mapWithID(id: String) = cached().maps.values
        .firstOrNull {
            it.name.equals(id, true)
        }

    fun mapWithSlime(id: String) = cached().maps.values
        .firstOrNull {
            it.associatedSlimeTemplate.equals(id, true)
        }

    var onPostReload = {}
    override fun postReload()
    {
        onPostReload()
        rebuildFastMapIndex()
    }
}
