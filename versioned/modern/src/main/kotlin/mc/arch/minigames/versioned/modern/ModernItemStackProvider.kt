package mc.arch.minigames.versioned.modern

import mc.arch.minigames.versioned.generics.ItemStackProvider
import org.bukkit.inventory.ItemStack

/**
 * Class created on 5/9/2026

 * @author Max C.
 * @project arch-minigames
 * @website https://solo.to/redis
 */
object ModernItemStackProvider : ItemStackProvider
{
    override fun makeUnbreakable(itemStack: ItemStack): ItemStack
    {
        val item = itemStack
        val meta = item.itemMeta ?: return item
        meta.isUnbreakable = true
        item.itemMeta = meta

        return item
    }
}
