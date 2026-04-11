package mc.arch.minigames.hungergames.kits

import com.cryptomorin.xseries.XMaterial
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * @author ArchMC
 */
data class HungerGamesKit(
    val id: String,
    var displayName: String = id,
    var icon: ItemStack = XMaterial.IRON_SWORD.parseItem()!!,
    val levels: MutableMap<Int, HungerGamesKitLevel> = mutableMapOf()
)
{
    fun maxLevel() = levels.keys.maxOrNull() ?: 0

    fun applyTo(player: Player, level: Int)
    {
        val kitLevel = levels[level.coerceAtMost(maxLevel()).coerceAtLeast(1)] ?: return

        kitLevel.armor.forEachIndexed { index, item ->
            if (item != null)
            {
                player.inventory.armorContents[index] = item.clone()
            }
        }
        player.inventory.armorContents = player.inventory.armorContents

        kitLevel.inventory.forEachIndexed { index, item ->
            if (item != null)
            {
                player.inventory.setItem(index, item.clone())
            }
        }

        player.updateInventory()
    }
}
