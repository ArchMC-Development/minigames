package gg.tropic.practice.minigame.menu

import com.cryptomorin.xseries.XMaterial
import gg.tropic.practice.map.MapService
import gg.tropic.practice.minigame.MiniGameModeMetadata
import gg.tropic.practice.minigame.MiniGameQueueConfiguration
import gg.tropic.practice.minigame.joinGame
import me.lucko.helper.Schedulers
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 6/24/25
 */
data class MinigameMapSelectorMenu(
    val onReturn: (Player) -> Unit,
    val mode: MiniGameModeMetadata
) : PaginatedMenu()
{
    init
    {
        applyHeader = false
    }

    override fun getMaxItemsPerPage(player: Player) = 21
    override fun size(buttons: Map<Int, Button>) = 45

    override fun getAllPagesButtonSlots() = (10..16) + (19..25) + (28..34)

    override fun getAllPagesButtons(player: Player) = MapService.cached().maps.values
        .filter { mode.mapGroup in it.associatedKitGroups && !it.locked }
        .mapIndexed { index, map ->
            index to ItemBuilder
                .of(XMaterial.OAK_SIGN)
                .name("${CC.GREEN}${map.displayName}")
                .addToLore(
                    "${CC.D_GRAY}${mode.displayName}",
                    "",
                    "${CC.GRAY}Map Selections: ${CC.GREEN}Unlimited ${CC.GRAY}(Beta)",
                    "",
                    "${CC.GREEN}Click to play!",
                    "${CC.YELLOW}Right-click to toggle favorite!",
                )
                .toButton { _, _ ->
                    player.closeInventory()
                    mode.joinGame(player, configuration = MiniGameQueueConfiguration(
                        requiredMapID = map.name
                    ))
                }
        }
        .associate { it.first to it.second }

    override fun getGlobalButtons(player: Player) = mapOf(
        39 to ItemBuilder.of(XMaterial.FIREWORK_ROCKET)
            .name("${CC.GOLD}Random")
            .addToLore(
                "${CC.D_GRAY}${mode.displayName}",
                "",
                "${CC.GRAY}Map Selections: ${CC.GREEN}Unlimited ${CC.GRAY}(Beta)",
                "",
                "${CC.GREEN}Click to play!"
            )
            .toButton { _, _ ->
                player.closeInventory()
                mode.joinGame(player, configuration = MiniGameQueueConfiguration(
                    requiredMapID = null
                ))
            },
        41 to ItemBuilder.of(XMaterial.DIAMOND)
            .name("${CC.AQUA}Random Favorite")
            .addToLore(
                "${CC.D_GRAY}${mode.displayName}",
                "",
                "${CC.GRAY}Map Selections: ${CC.GREEN}Unlimited ${CC.GRAY}(Beta)",
                "",
                "${CC.GREEN}Click to play!"
            )
            .toButton { _, _ ->
                player.sendMessage("${CC.RED}Coming soon!")
            }
    )

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose)
        {
            Schedulers.sync().run {
                onReturn(player)
            }
        }
    }

    override fun getPrePaginatedTitle(player: Player) = "${mode.displayName} Map Selector"
}
