package mc.arch.minigames.persistent.housing.game

import mc.arch.minigames.persistent.housing.api.content.HousingItemStack
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * Class created on 12/18/2025

 * @author Max C.
 * @project arch-minigames
 * @website https://solo.to/redis
 */
fun HousingItemStack.toBukkitStack(): ItemStack = ItemStack(Material.valueOf(this.material), this.amount)
    .also { itemStack ->
        val itemMeta = itemStack.itemMeta
            ?: Bukkit.getItemFactory().getItemMeta(itemStack.type)

        itemMeta.displayName = this.displayName
        itemMeta.lore = this.description

        itemStack.durability = this.data
        itemStack.itemMeta = itemMeta
    }