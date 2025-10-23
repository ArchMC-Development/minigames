package gg.tropic.practice.menu.manage.lobbynpc

import com.cryptomorin.xseries.XMaterial
import gg.tropic.practice.configuration.PracticeConfigurationService
import gg.tropic.practice.configuration.minigame.MinigameLobbyNPC
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
class ViewMinigameLobbyNPCsMenu : PaginatedMenu()
{
    init
    {
        placeholdBorders = true
    }

    override fun getPrePaginatedTitle(player: Player) = "Managing Lobby NPCs"
    override fun getAllPagesButtonSlots() = listOf(
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25
    )

    override fun getGlobalButtons(player: Player) =
        mutableMapOf(
            4 to ItemBuilder.Companion
                .of(XMaterial.WRITABLE_BOOK)
                .name("${CC.B_YELLOW}Create a Lobby NPC")
                .addToLore(
                    "",
                    "${CC.YELLOW}Click to create!"
                )
                .toButton { _, _ ->
                    PracticeConfigurationService.editAndSave {
                        local().minigameLobbyNPCs += MinigameLobbyNPC()
                    }

                    Button.playNeutral(player)
                    player.closeInventory()
                    player.sendMessage("${CC.GREEN}Created!")
                }
        )

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        PracticeConfigurationService.cached().local().minigameLobbyNPCs
            .forEachIndexed { index, npc ->
                buttons[index] = ItemBuilder
                    .of(XMaterial.VILLAGER_SPAWN_EGG)
                    .name("${CC.B_YELLOW}#${index + 1}")
                    .addToLore(
                        "${CC.YELLOW}Position: ${CC.WHITE}${npc.position}",
                        "${CC.YELLOW}Command: ${CC.WHITE}${npc.command}",
                        "${CC.YELLOW}Gamemode Name: ${CC.WHITE}${npc.gamemodeName}",
                        "${CC.YELLOW}Player Count Replacement: ${CC.WHITE}${npc.replacement}",
                        "${CC.YELLOW}Skin Type: ${CC.WHITE}${npc.skinType}",
                        "${CC.YELLOW}Held Item Type: ${CC.WHITE}${npc.heldItem.type}",
                        "",
                        "${CC.GREEN}Click to configure!"
                    )
                    .toButton { _, _ ->
                        ManageMinigameLobbyNPCMenu(index).openMenu(player)
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
