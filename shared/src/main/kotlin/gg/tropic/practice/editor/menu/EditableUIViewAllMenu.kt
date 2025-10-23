package gg.tropic.practice.editor.menu

import com.cryptomorin.xseries.XMaterial
import gg.scala.commons.util.prompts.collectResponses
import gg.tropic.practice.configuration.PracticeConfigurationService
import gg.tropic.practice.editor.EditableUI
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 7/5/25
 */
class EditableUIViewAllMenu : PaginatedMenu()
{
    init
    {
        placeholdBorders = true
    }

    override fun getPrePaginatedTitle(player: Player) = "Managing Editable UIs"
    override fun getAllPagesButtonSlots() = listOf(
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25
    )

    override fun getGlobalButtons(player: Player) =
        mutableMapOf(
            4 to ItemBuilder.Companion
                .of(XMaterial.WRITABLE_BOOK)
                .name("${CC.B_YELLOW}Create an Editable UI")
                .addToLore(
                    "",
                    "${CC.YELLOW}Click to create!"
                )
                .toButton { _, _ ->
                    Button.playNeutral(player)
                    player.closeInventory()
                    player.collectResponses {
                        with("id", "ID")
                    }.subscribe({ responses ->
                        PracticeConfigurationService.editAndSave {
                            editableUIs[responses["id"]!!] = EditableUI(id = responses["id"]!!)
                        }

                        openMenu(player)
                    }, {

                    })
                }
        )

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        PracticeConfigurationService.cached().editableUIs.values
            .forEachIndexed { index, id ->
                buttons[index] = ItemBuilder
                    .of(XMaterial.OAK_SIGN)
                    .name("${CC.B_YELLOW}${id.id}")
                    .addToLore(
                        "${CC.YELLOW}Title: ${CC.WHITE}${id.title}",
                        "${CC.YELLOW}Style: ${CC.WHITE}${id.style}",
                        "${CC.YELLOW}Type: ${CC.WHITE}${id.inventoryType}",
                        "${CC.YELLOW}Size: ${CC.WHITE}${id.size}",
                        "",
                        "${CC.YELLOW}Auto Update: ${CC.WHITE}${if (id.autoUpdate) "${CC.GREEN}Yes" else "${CC.RED}No"}",
                        "",
                        "${CC.GREEN}Click to configure!"
                    )
                    .toButton { _, _ ->
                        ManageEditableUIMenu(id.id).openMenu(player)
                    }
            }

        return buttons
    }

    override fun size(buttons: Map<Int, Button>) = 36
    override fun getMaxItemsPerPage(player: Player) = 14
}
