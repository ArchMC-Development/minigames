package gg.tropic.practice.editor.menu

import com.cryptomorin.xseries.XMaterial
import gg.scala.commons.configurable.*
import gg.tropic.practice.configuration.PracticeConfiguration
import gg.tropic.practice.configuration.PracticeConfigurationService
import gg.tropic.practice.editor.UIItemAction
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.Color
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 6/27/25
 */
class ManageEditableUIButtonMenu(
    private val uiId: String,
    private val buttonSlot: Int
) : Menu("UI Button: $uiId, Slot $buttonSlot")
{
    init
    {
        placeholder = true
    }

    override fun getButtons(player: Player) = mapOf(
        10 to editString(
            PracticeConfigurationService,
            title = "Display Name",
            material = XMaterial.NAME_TAG,
            getter = {
                editableUIs[uiId]!!.buttons[buttonSlot]!!.displayName
            },
            setter = {
                editableUIs[uiId]!!.buttons[buttonSlot]!!.displayName = Color.translate(it)
            }
        ),
        11 to editItemStack(
            PracticeConfigurationService,
            title = "Icon",
            getter = {
                editableUIs[uiId]!!.buttons[buttonSlot]!!.icon
            },
            setter = {
                editableUIs[uiId]!!.buttons[buttonSlot]!!.icon = it
            }
        ),
        12 to editInt(
            PracticeConfigurationService,
            title = "Amount",
            material = XMaterial.PAPER,
            getter = {
                editableUIs[uiId]!!.buttons[buttonSlot]!!.amount
            },
            setter = {
                editableUIs[uiId]!!.buttons[buttonSlot]!!.amount = it
            },
            range = 1..64
        ),
        13 to editBoolean(
            PracticeConfigurationService,
            title = "Close Inventory",
            material = XMaterial.BARRIER,
            getter = {
                editableUIs[uiId]!!.buttons[buttonSlot]!!.closeInventory
            },
            setter = {
                editableUIs[uiId]!!.buttons[buttonSlot]!!.closeInventory = it
            }
        ),
        14 to editStringList(
            PracticeConfigurationService,
            title = "Lore",
            material = XMaterial.WRITABLE_BOOK,
            getter = {
                editableUIs[uiId]!!.buttons[buttonSlot]!!.lore
            },
            setter = {
                editableUIs[uiId]!!.buttons[buttonSlot]!!.lore = it.map { line -> Color.translate(line) }
            }
        ),
        15 to editEnum<UIItemAction, PracticeConfiguration>(
            PracticeConfigurationService,
            title = "Action",
            material = XMaterial.REDSTONE,
            getter = {
                editableUIs[uiId]!!.buttons[buttonSlot]!!.action
            },
            setter = {
                editableUIs[uiId]!!.buttons[buttonSlot]!!.action = it as UIItemAction
            }
        ),
        16 to editStringList(
            PracticeConfigurationService,
            title = "Action Data",
            material = XMaterial.COMMAND_BLOCK,
            getter = {
                editableUIs[uiId]!!.buttons[buttonSlot]!!.actionData
            },
            setter = {
                editableUIs[uiId]!!.buttons[buttonSlot]!!.actionData = it.map { line -> Color.translate(line) }
            }
        )
    )

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose)
        {
            Tasks.sync {
                ManageEditableUIButtonsMenu(uiId).openMenu(player)
            }
        }
    }

    override fun size(buttons: Map<Int, Button>) = 27
}
