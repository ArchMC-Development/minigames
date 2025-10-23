package gg.tropic.practice.games

import com.cryptomorin.xseries.XMaterial
import gg.tropic.practice.kit.Kit
import gg.tropic.practice.statistics.LocalAccumulator
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * @author GrowlyX
 * @since 8/9/2022
 */
class GameReportSnapshot(player: Player, counter: LocalAccumulator, kit: Kit)
{
    val playerUniqueId = player.uniqueId
    val inventoryContents: Array<ItemStack?> = player.inventory.contents

    val armorContents = player.inventory.armorContents
    val potionEffects = player.activePotionEffects

    val healthPotions = player.inventory.contents
        .filterNotNull()
        .count {
            it.type == Material.POTION && it.durability.toInt() == 16421
        }

    val containsHealthPotions = kit.contents
        .filterNotNull()
        .any {
            it.type == Material.POTION && it.durability.toInt() == 16421
        }

    val missedPotions = counter.valueOf("missedPots").toInt()
    val wastedHeals = counter.valueOf("wastedHeals")

    val hitPotions = counter.valueOf("hitPots").toInt()
    val totalPotionsUsed = counter.valueOf("totalPots").toInt()

    val mushroomStews = player.inventory.contents
        .filterNotNull()
        .count {
            it.type == XMaterial.MUSHROOM_STEW.parseMaterial()
        }

    val containsMushroomStews = kit.contents
        .filterNotNull()
        .any {
            it.type == XMaterial.MUSHROOM_STEW.parseMaterial()
        }

    val health = player.health
    val foodLevel = player.foodLevel
}
