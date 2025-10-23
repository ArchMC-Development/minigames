package mc.arch.minigames.versioned.legacy

import mc.arch.minigames.versioned.generics.PotionProvider
import net.evilblock.cubed.util.CC
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.Potion
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionType

/**
 * @author Subham
 * @since 8/5/25
 */
object LegacyPotionProvider : PotionProvider
{
    override fun buildPotion(
        type: PotionType,
        name: String,
        splash: Boolean,
        amount: Int,
        level: Int,
        duration: Int,
        customEffects: List<PotionEffect>
    ): ItemStack
    {
        val potion = Potion(type)
        if (splash)
        {
            potion.splash()
        }

        val itemStack = potion.toItemStack(amount)

        val meta = itemStack.itemMeta as PotionMeta
        meta.displayName = "${CC.WHITE}$name"
        if (customEffects.isEmpty())
        {
            meta.addCustomEffect(
                PotionEffect(type.effectType, duration, level),
                true
            )
        } else
        {
            customEffects.forEach { effect ->
                meta.addCustomEffect(effect, true)
            }
        }

        itemStack.itemMeta = meta
        return itemStack
    }
}
