package gg.tropic.practice.application.api.defaults.map

import gg.scala.commons.persist.datasync.DataSyncKeys
import gg.scala.commons.persist.datasync.DataSyncService
import gg.scala.commons.persist.datasync.DataSyncSource
import gg.tropic.practice.application.api.defaults.kit.ImmutableKit
import gg.tropic.practice.application.api.defaults.kit.group.ImmutableKitGroup
import gg.tropic.practice.application.api.defaults.kit.group.KitGroupDataSync
import gg.tropic.practice.namespace
import gg.tropic.practice.namespaceShortened
import gg.tropic.practice.provider.MiniProviderVersion
import gg.tropic.practice.suffixWhenDev
import net.kyori.adventure.key.Key

/**
 * @author GrowlyX
 * @since 9/24/2023
 */
object MapDataSync : DataSyncService<ImmutableMapContainer>()
{
    object DPSMapKeys : DataSyncKeys
    {
        override fun newStore() = "mi-practice-maps"

        override fun store() = Key.key(namespace(), "maps")
        override fun sync() = Key.key(namespaceShortened().suffixWhenDev(), "msync")
    }

    override fun locatedIn() = DataSyncSource.Mongo

    override fun keys() = DPSMapKeys
    override fun type() = ImmutableMapContainer::class.java

    /**
     * Modern providers can host LEGACY-authored maps via ASP's SWM compat shim;
     * legacy providers cannot host MODERN maps because 1.8 has no concept of post-1.8 blocks.
     */
    private fun ImmutableMap.runsOn(provider: MiniProviderVersion) = when (provider)
    {
        MiniProviderVersion.LEGACY -> version == MiniProviderVersion.LEGACY
        MiniProviderVersion.MODERN -> true
    }

    fun selectMapIfCompatible(
        kit: ImmutableKit,
        mapID: String?,
        provider: MiniProviderVersion = MiniProviderVersion.LEGACY
    ): ImmutableMap?
    {
        val groups = KitGroupDataSync.groupsOf(kit)
            .map(ImmutableKitGroup::id)

        return cached().maps.values
            .filterNot(ImmutableMap::locked)
            .filter { it.runsOn(provider) }
            .filter { groups.intersect(it.associatedKitGroups).isNotEmpty() }
            .shuffled()
            .firstOrNull { if (mapID == null) true else it.name == mapID }
    }

    fun selectRandomMapCompatibleWith(
        kit: ImmutableKit,
        provider: MiniProviderVersion = MiniProviderVersion.LEGACY
    ): ImmutableMap?
    {
        val groups = KitGroupDataSync.groupsOf(kit)
            .map(ImmutableKitGroup::id)

        return cached().maps.values
            .filterNot(ImmutableMap::locked)
            .filter { it.runsOn(provider) }
            .shuffled()
            .firstOrNull {
                groups.intersect(it.associatedKitGroups).isNotEmpty()
            }
    }
}
