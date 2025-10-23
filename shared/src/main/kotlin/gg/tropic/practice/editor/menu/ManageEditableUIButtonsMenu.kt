package gg.tropic.practice.editor.menu

import com.cryptomorin.xseries.XMaterial
import gg.tropic.practice.configuration.PracticeConfigurationService
import gg.tropic.practice.editor.EditableUIButton
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.menu.menus.ConfirmMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 6/27/25
 */
class ManageEditableUIButtonsMenu(
    private val uiId: String
) : Menu("Managing UI Buttons: $uiId")
{
    override fun getButtons(player: Player) = PracticeConfigurationService
        .cached().editableUIs[uiId]!!
        .compose()
        .getButtons(player)
        .mapValues {
            val uiItem = PracticeConfigurationService
                .cached().editableUIs[uiId]!!
                .buttons[it.key]

            val buttonItem = it.value.getButtonItem(player)
            if (uiItem != null)
            {
                return@mapValues ItemBuilder.copyOf(buttonItem)
                    .addToLore(
                        "",
                        "${CC.GRAY}${CC.STRIKE_THROUGH}${" ".repeat(20)}",
                        "${CC.B_GREEN}UI ITEM SET",
                        "${CC.YELLOW}Close Inv.: ${
                            if (uiItem.closeInventory) "${CC.GREEN}Yes" else "${CC.RED}No"
                        }",
                        "${CC.YELLOW}Action: ${CC.WHITE}${uiItem.action}",
                        *uiItem.actionData
                            .map {
                                "${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.WHITE}$it"
                            }
                            .toTypedArray(),
                        "",
                        "${CC.B_AQUA}LEFT-CLICK TO CONFIGURE",
                        "${CC.B_AQUA}RIGHT-CLICK TO DELETE",
                        "${CC.GRAY}${CC.STRIKE_THROUGH}${" ".repeat(20)}",
                    )
                    .toButton { _, type ->
                        Button.playNeutral(player)

                        if (type!!.isRightClick)
                        {
                            ConfirmMenu { confirmed ->
                                if (confirmed)
                                {
                                    PracticeConfigurationService.editAndSave {
                                        editableUIs[uiId]!!.buttons.remove(it.key)
                                    }
                                }

                                openMenu(player)
                            }.openMenu(player)
                            return@toButton
                        }

                        ManageEditableUIButtonMenu(uiId, it.key).openMenu(player)
                    }
            }

            return@mapValues ItemBuilder
                .copyOf(buttonItem)
                .name("${CC.B_RED}NO UI ITEM SET")
                .addToLore(
                    "${CC.RED}Click to set item!"
                )
                .toButton { _, _ ->
                    Button.playNeutral(player)
                    PracticeConfigurationService.editAndSave {
                        editableUIs[uiId]!!.buttons.put(it.key, EditableUIButton())
                    }
                    openMenu(player)
                }
        }
        .toMutableMap()
        .apply {
            (0..(PracticeConfigurationService
                .cached().editableUIs[uiId]!!.size - 1))
                .forEach { slot ->
                    if (this[slot] == null)
                    {
                        this[slot] = ItemBuilder
                            .of(XMaterial.BARRIER)
                            .name("${CC.B_RED}NO UI ITEM SET")
                            .addToLore(
                                "${CC.RED}Click to set item!"
                            )
                            .toButton { _, _ ->
                                Button.playNeutral(player)
                                PracticeConfigurationService.editAndSave {
                                    editableUIs[uiId]!!.buttons.put(slot, EditableUIButton())
                                }
                                openMenu(player)
                            }
                    }
                }
        }

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose)
        {
            Tasks.sync {
                ManageEditableUIMenu(uiId).openMenu(player)
            }
        }
    }

    override fun size(buttons: Map<Int, Button>) = PracticeConfigurationService
        .cached().editableUIs[uiId]!!
        .compose()
        .size(buttons)
}
