package gg.tropic.practice.menu.manage.bezierteleporters

import com.cryptomorin.xseries.XMaterial
import gg.tropic.practice.configuration.PracticeConfigurationService
import gg.tropic.practice.configuration.minigame.MinigameBezierTeleporter
import gg.tropic.practice.configuration.minigame.MinigamePlayNPC
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
class ViewBezierTeleportersMenu : PaginatedMenu()
{
    init
    {
        placeholdBorders = true
    }

    override fun getPrePaginatedTitle(player: Player) = "Managing Bezier Teleporters"
    override fun getAllPagesButtonSlots() = listOf(
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25
    )

    override fun getGlobalButtons(player: Player) =
        mutableMapOf(
            4 to ItemBuilder.Companion
                .of(XMaterial.WRITABLE_BOOK)
                .name("${CC.D_AQUA}Create a Bezier Teleporter")
                .addToLore(
                    "",
                    "${CC.YELLOW}Click to create!"
                )
                .toButton { _, _ ->
                    PracticeConfigurationService.editAndSave {
                        local().bezierTeleporters += MinigameBezierTeleporter()
                    }

                    Button.playNeutral(player)
                    player.closeInventory()
                    player.sendMessage("${CC.GREEN}Created!")
                }
        )

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        PracticeConfigurationService.cached().local().bezierTeleporters
            .forEachIndexed { index, npc ->
                buttons[index] = ItemBuilder
                    .of(XMaterial.FEATHER)
                    .name("${CC.B_YELLOW}#${index + 1}")
                    .addToLore(
                        "${CC.YELLOW}Start: ${CC.WHITE}${npc.start}",
                        "${CC.YELLOW}End: ${CC.WHITE}${npc.end}",
                        "",
                        "${CC.YELLOW}Height: ${CC.WHITE}${npc.height}",
                        "${CC.YELLOW}Duration (Ticks): ${CC.WHITE}${npc.duration}",
                        "${CC.YELLOW}Motion Preset: ${CC.WHITE}${npc.preset}",
                        "",
                        "${CC.GREEN}Click to configure!"
                    )
                    .toButton { _, _ ->
                        ManageBezierTeleporterMenu(index).openMenu(player)
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
