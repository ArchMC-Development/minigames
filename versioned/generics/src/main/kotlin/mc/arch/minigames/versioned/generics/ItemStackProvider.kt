package mc.arch.minigames.versioned.generics

import org.bukkit.inventory.ItemStack

/**
 * Class created on 5/9/2026

 * @author Max C.
 * @project arch-minigames
 * @website https://solo.to/redis
 */
interface ItemStackProvider
{
    fun makeUnbreakable(itemStack: ItemStack): ItemStack
}
