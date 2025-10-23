package gg.tropic.practice.games.loadout

import com.cryptomorin.xseries.XMaterial
import gg.tropic.practice.games.GameService
import gg.tropic.practice.kit.feature.FeatureFlag
import gg.tropic.practice.extensions.toRBTeamSide
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * @author GrowlyX
 * @since 7/19/2024
 */
fun Player.fixInventoryBasedOnRBTeamColor()
{
    val game = GameService.byPlayer(this)
    if (game != null)
    {
        val team = game.getTeamOf(player)
        if (game.flag(FeatureFlag.RedBlueTeams))
        {
            val teamSide = team.teamIdentifier.toRBTeamSide()
            player.inventory.contents.indices.forEach { index ->
                val itemStack = player.inventory.contents[index]
                    ?: return@forEach

                if (itemStack.type == XMaterial.WHITE_WOOL.parseMaterial() ||
                    itemStack.type == XMaterial.BLACK_TERRACOTTA.parseMaterial())
                {
                    player.inventory.setItem(
                        index,
                        ItemBuilder
                            .copyOf(itemStack)
                            .data(teamSide.blockColor)
                            .build()
                    )
                }
            }

            val newArrayOfContents = arrayOf<ItemStack?>(null, null, null, null)
            player.inventory.armorContents.indices.forEach { index ->
                val itemStack = player.inventory.armorContents[index]
                    ?: return@forEach

                if (!itemStack.type.name.contains("LEATHER"))
                {
                    newArrayOfContents[index] = itemStack
                    return@forEach
                }

                newArrayOfContents[index] = ItemBuilder
                    .copyOf(itemStack)
                    .color(teamSide.armorColor)
                    .build()
            }

            player.inventory.armorContents = newArrayOfContents
        }
    }
}
