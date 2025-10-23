package gg.tropic.practice.portal.menu

import com.cryptomorin.xseries.XMaterial
import gg.tropic.practice.portal.LobbyPortalService
import gg.tropic.practice.portal.procedure.PortalCreationProcedure
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 4/4/25
 */
class ViewLobbyPortalsMenu : PaginatedMenu()
{
    init
    {
        placeholdBorders = true
    }

    override fun getPrePaginatedTitle(player: Player) = "Managing Lobby Portals"
    override fun getAllPagesButtonSlots() = listOf(
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25
    )

    override fun getGlobalButtons(player: Player) =
        mutableMapOf(
            4 to ItemBuilder.Companion
                .of(XMaterial.WRITABLE_BOOK)
                .name("${CC.B_YELLOW}Create a Lobby Portal")
                .addToLore(
                    "",
                    "${CC.YELLOW}Click to create!"
                )
                .toButton { _, _ ->
                    PortalCreationProcedure.start(player)
                    Button.playNeutral(player)
                    player.closeInventory()
                }
        )

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        LobbyPortalService.cached().portals.values
            .forEachIndexed { index, portals ->
                buttons[index] = ItemBuilder
                    .of(XMaterial.EGG)
                    .name("${CC.B_YELLOW}#${index + 1}")
                    .addToLore(
                        "${CC.YELLOW}Queue Destination: ${CC.WHITE}${portals.destination}",
                        "${CC.YELLOW}Scope: ${CC.WHITE}${portals.server}",
                        "${CC.YELLOW}Blocks: ${CC.WHITE}${portals.blocks.size}",
                        "",
                        "${CC.GREEN}Click to configure!"
                    )
                    .toButton { _, _ ->
                        ManageLobbyPortalMenu(portals.identifier).openMenu(player)
                    }
            }

        return buttons
    }

    override fun size(buttons: Map<Int, Button>) = 36
    override fun getMaxItemsPerPage(player: Player) = 14
}
