package gg.tropic.practice.menu.manage.npc

import com.cryptomorin.xseries.XMaterial
import gg.tropic.practice.configuration.PracticeConfigurationService
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
class ViewMinigameNPCsMenu : PaginatedMenu()
{
    init
    {
        placeholdBorders = true
    }

    override fun getPrePaginatedTitle(player: Player) = "Managing Play NPCs"
    override fun getAllPagesButtonSlots() = listOf(
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25
    )

    override fun getGlobalButtons(player: Player) =
        mutableMapOf(
            4 to ItemBuilder.Companion
                .of(XMaterial.WRITABLE_BOOK)
                .name("${CC.D_AQUA}Create a Minigame NPC")
                .addToLore(
                    "",
                    "${CC.YELLOW}Click to create!"
                )
                .toButton { _, _ ->
                    PracticeConfigurationService.editAndSave {
                        local().playNPCs += MinigamePlayNPC()
                    }

                    Button.playNeutral(player)
                    player.closeInventory()
                    player.sendMessage("${CC.GREEN}Created!")
                }
        )

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        PracticeConfigurationService.cached().local().playNPCs.forEachIndexed { index, npc ->
            buttons[index] = ItemBuilder
                .of(XMaterial.EGG)
                .name("${CC.B_YELLOW}#${index + 1}")
                .addToLore(
                    "${CC.YELLOW}Position: ${CC.WHITE}${npc.position}",
                    "${CC.YELLOW}Game Mode: ${CC.WHITE}${npc.associatedGameMode}",
                    "",
                    "${CC.GREEN}Click to configure!"
                )
                .toButton { _, _ ->
                    ManageMinigamePlayNPCMenu(index).openMenu(player)
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
