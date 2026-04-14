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
    var selectedKitLevel: Int = 1,
    val purchasedKits: MutableMap<String, MutableSet<Int>> = mutableMapOf()
) : IDataStoreObject
{
    /**
     * Check if the player owns a specific kit level.
     * Level 1 of every kit is free by default.
     */
    fun hasKit(kitId: String, level: Int): Boolean
    {
        if (level <= 1) return true
        return purchasedKits[kitId]?.contains(level) == true
    }

    /**
     * Check if the player owns any level of a kit (at minimum level 1, which is always free).
     */
    fun ownsAnyLevel(kitId: String): Boolean = true

    /**
     * Unlock a specific kit level for this player.
     */
    fun unlockKit(kitId: String, level: Int)
    {
        purchasedKits.getOrPut(kitId) { mutableSetOf() }.add(level)
    }

    /**
     * Get the highest level the player owns for a kit.
     */
    fun highestOwnedLevel(kitId: String, maxLevel: Int): Int
    {
        val owned = purchasedKits[kitId] ?: return 1
        return (1..maxLevel).lastOrNull { it == 1 || it in owned } ?: 1
    }

    fun save() = DataStoreObjectControllerCache
        .findNotNull<HungerGamesProfile>()
        .save(this, DataStoreStorageType.MONGO)
}

