package gg.tropic.practice.menu.manage.levitationportals

import com.cryptomorin.xseries.XMaterial
import gg.scala.commons.spatial.Bounds
import gg.scala.commons.spatial.Position
import gg.tropic.practice.configuration.PracticeConfigurationService
import gg.tropic.practice.configuration.minigame.levitationportal.LevitationPortalSpec
import gg.tropic.practice.menu.manage.ManageLobbyMenu
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 4/4/25
 */
class ViewLevitationPortalsMenu : PaginatedMenu()
{
    init
    {
        placeholdBorders = true
    }

    override fun getPrePaginatedTitle(player: Player) = "Managing Levitation Portals"
    override fun getAllPagesButtonSlots() = listOf(
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25
    )

    override fun getGlobalButtons(player: Player) =
        mutableMapOf(
            4 to ItemBuilder.Companion
                .of(XMaterial.WRITABLE_BOOK)
                .name("${CC.D_AQUA}Create a Levitation Portal")
                .addToLore(
                    "",
                    "${CC.YELLOW}Click to create!"
                )
                .toButton { _, _ ->
                    PracticeConfigurationService.editAndSave {
                        local().levitationPortals += LevitationPortalSpec(
                            id = "new-portal",
                            bounds = Bounds(
                                Position(0.0, 0.0, 0.0),
                                Position(0.0, 0.0, 0.0)
                            )
                        )
                    }

                    Button.playNeutral(player)
                    player.closeInventory()
                    player.sendMessage("${CC.GREEN}Created!")
                }
        )

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        PracticeConfigurationService.cached().local().levitationPortals
            .forEachIndexed { index, portal ->
                buttons[index] = ItemBuilder
                    .of(XMaterial.EGG)
                    .name("${CC.B_YELLOW}#${index + 1} ${CC.GRAY}(${portal.id})")
                    .addToLore(
                        "${CC.YELLOW}Bounds: ${CC.WHITE}${portal.bounds}",
                        "",
                        "${CC.YELLOW}Height: ${CC.WHITE}${portal.height}",
                        "${CC.YELLOW}Limit: ${CC.WHITE}${portal.limit}",
                        "${CC.YELLOW}Restrict Below Air: ${CC.WHITE}${portal.restrictBelowAir}",
                        "",
                        "${CC.GREEN}Click to configure!"
                    )
                    .toButton { _, _ ->
                        ManageLevitationPortalsMenu(index).openMenu(player)
                    }
            }

        return buttons
    }

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose)
        {
            Tasks.sync {
                ManageLobbyMenu().openMenu(player)
            }
        }
    }

    override fun size(buttons: Map<Int, Button>) = 36
    override fun getMaxItemsPerPage(player: Player) = 14
}
