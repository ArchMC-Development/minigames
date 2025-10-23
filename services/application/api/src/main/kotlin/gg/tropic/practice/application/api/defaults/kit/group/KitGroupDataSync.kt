package gg.tropic.practice.application.api.defaults.kit.group

import gg.scala.commons.persist.datasync.DataSyncKeys
import gg.scala.commons.persist.datasync.DataSyncService
import gg.scala.commons.persist.datasync.DataSyncSource
import gg.tropic.practice.application.api.defaults.kit.ImmutableKit
import gg.tropic.practice.namespace
import gg.tropic.practice.namespaceShortened
import gg.tropic.practice.suffixWhenDev
import net.kyori.adventure.key.Key

/**
 * @author GrowlyX
 * @since 9/24/2023
 */
object KitGroupDataSync : DataSyncService<ImmutableKitContainer>()
{
    object DPSKitGroupKeys : DataSyncKeys
    {
        override fun newStore() = "mi-practice-kits-groups"

        override fun store() = Key.key(namespace(), "groups")
        override fun sync() = Key.key(namespaceShortened().suffixWhenDev(), "gsync")
    }

    override fun locatedIn() = DataSyncSource.Mongo

    override fun keys() = DPSKitGroupKeys
    override fun type() = ImmutableKitContainer::class.java

    fun groupsOf(kit: ImmutableKit) = cached().backingGroups
        .filter {
            kit.id in it.contains
        }
}
