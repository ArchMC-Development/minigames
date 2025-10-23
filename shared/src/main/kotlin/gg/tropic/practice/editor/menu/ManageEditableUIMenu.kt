package gg.tropic.practice.editor.menu

import com.cryptomorin.xseries.XMaterial
import gg.scala.commons.configurable.editBoolean
import gg.scala.commons.configurable.editEnum
import gg.scala.commons.configurable.editInt
import gg.scala.commons.configurable.editString
import gg.tropic.practice.configuration.PracticeConfiguration
import gg.tropic.practice.configuration.PracticeConfigurationService
import gg.tropic.practice.editor.UIStyle
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.Color
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType

/**
 * @author Subham
 * @since 6/27/25
 */
class ManageEditableUIMenu(
    private val id: String
) : Menu("Managing Editable UI: $id")
{
    init
    {
        placeholder = true
    }

    override fun getButtons(player: Player) = mapOf(
        10 to editString(
            PracticeConfigurationService,
            title = "Title",
            material = XMaterial.NAME_TAG,
            getter = {
                editableUIs[id]!!.title
            },
            setter = {
                editableUIs[id]!!.title = Color.translate(it)
            }
        ),
        12 to editInt(
            PracticeConfigurationService,
            title = "Size",
            material = XMaterial.CHEST,
            getter = {
                editableUIs[id]!!.size
            },
            setter = {
                editableUIs[id]!!.size = it
            },
            range = 9..54
        ),
        13 to editEnum<UIStyle, PracticeConfiguration>(
            PracticeConfigurationService,
            title = "UI Style",
            material = XMaterial.ITEM_FRAME,
            getter = {
                editableUIs[id]!!.style
            },
            setter = {
                editableUIs[id]!!.style = it as UIStyle
            }
        ),
        14 to editEnum<InventoryType, PracticeConfiguration>(
            PracticeConfigurationService,
            title = "Inventory Type",
            material = XMaterial.CHEST,
            getter = {
                editableUIs[id]!!.inventoryType
            },
            setter = {
                editableUIs[id]!!.inventoryType = it as InventoryType
            }
        ),
        15 to editBoolean(
            PracticeConfigurationService,
            title = "Auto Update",
            material = XMaterial.CLOCK,
            getter = {
                editableUIs[id]!!.autoUpdate
            },
            setter = {
                editableUIs[id]!!.autoUpdate = it
            }
        ),
        16 to ItemBuilder
            .of(XMaterial.WHITE_WOOL)
            .name("${CC.YELLOW}Buttons")
            .addToLore(
                "${CC.GRAY}Click to edit."
            )
            .toButton { _, _ ->
                ManageEditableUIButtonsMenu(id).openMenu(player)
            },
        4 to ItemBuilder
            .of(XMaterial.NETHER_STAR)
            .name("${CC.B_YELLOW}Preview")
            .toButton { _, _ ->
                Button.playNeutral(player)
                PracticeConfigurationService.openEditableUI(player, id)
            }
    )

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose)
        {
            Tasks.sync {
                EditableUIViewAllMenu().openMenu(player)
            }
        }
    }

    override fun size(buttons: Map<Int, Button>) = 27
}
