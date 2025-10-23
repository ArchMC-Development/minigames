package mc.arch.minigames.versioned.modern

import com.cryptomorin.xseries.XMaterial
import mc.arch.minigames.versioned.generics.PotionProvider
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionType

/**
 * @author Subham
 * @since 8/5/25
 */
object ModernPotionProvider : PotionProvider
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
        val potion = ItemBuilder
            .of(if (!splash) XMaterial.POTION else XMaterial.SPLASH_POTION)
            .amount(amount)
            .build()

        val meta = potion.itemMeta as PotionMeta
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

        potion.itemMeta = meta
        return potion
    }
}
