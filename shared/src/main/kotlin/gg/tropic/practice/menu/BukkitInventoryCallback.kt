package gg.tropic.practice.menu

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class BukkitInventoryCallback(
    private val contentsToSet: Array<ItemStack?>,
    val immutableSlots: List<Int>,
    private val title: String,
    val size: Int,
    val callback: (Array<ItemStack?>) -> Unit
)
{
    fun openMenu(player: Player)
    {
        val inventory = Bukkit.createInventory(null, size, title)
        inventory.contents = contentsToSet

        player.openInventory(inventory)
        InventoryEventsListener.inventoryMap[player.uniqueId] = this
    }
}
