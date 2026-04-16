package gg.tropic.practice.menu

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Represents a clickable action button in a [BukkitInventoryCallback] menu.
 */
data class CallbackButton(
    val item: ItemStack,
    val onClick: (Player) -> Unit
)

class BukkitInventoryCallback(
    private val contentsToSet: Array<ItemStack?>,
    val immutableSlots: List<Int>,
    private val title: String,
    val size: Int,
    val buttons: Map<Int, CallbackButton> = emptyMap(),
    val callback: ((Array<ItemStack?>) -> Unit)? = null,
    val suppressCloseCallback: Boolean = false
)
{
    /**
     * Whether the close callback should be skipped on the next close.
     * Set to true when a button action handles saving/closing itself.
     */
    @Transient
    var skipCloseCallback = false

    fun openMenu(player: Player)
    {
        val inventory = Bukkit.createInventory(null, size, title)
        inventory.contents = contentsToSet

        // Place button items into the inventory
        for ((slot, button) in buttons)
        {
            inventory.setItem(slot, button.item)
        }

        player.openInventory(inventory)
        InventoryEventsListener.inventoryMap[player.uniqueId] = this
    }
}
