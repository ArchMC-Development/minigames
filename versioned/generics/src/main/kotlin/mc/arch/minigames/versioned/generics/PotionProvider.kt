package mc.arch.minigames.versioned.generics

import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionType

/**
 * @author Subham
 * @since 8/5/25
 */
interface PotionProvider
{
    fun buildPotion(
        type: PotionType,
        name: String,
        splash: Boolean,
        amount: Int,
        level: Int,
        duration: Int,
        customEffects: List<PotionEffect>
    ): ItemStack
}
