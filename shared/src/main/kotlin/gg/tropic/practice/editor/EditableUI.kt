package gg.tropic.practice.editor

import gg.tropic.practice.replacements.toTemplatePlayerCounts
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemFlag

/**
 * @author Subham
 * @since 7/5/25
 */
data class EditableUI(
    val id: String,
    var size: Int = 27,
    var title: String = "Example Editable UI",
    var style: UIStyle = UIStyle.FILLED,
    var inventoryType: InventoryType = InventoryType.CHEST,
    var autoUpdate: Boolean = true,
    val buttons: MutableMap<Int, EditableUIButton> = mutableMapOf()
)
{
    fun compose() = object : Menu(title)
    {
        override fun asyncLoadResources(player: Player, callback: (Boolean) -> Unit)
        {
            callback(true)
        }

        override fun getButtons(player: Player) = this@EditableUI.buttons
            .mapValues { entry ->
                ItemBuilder
                    .copyOf(entry.value.icon)
                    .name(entry.value.displayName)
                    .setLore(
                        entry.value.lore.map { text ->
                            text.toTemplatePlayerCounts()
                        }
                    )
                    .amount(entry.value.amount)
                    .addFlags(
                        ItemFlag.HIDE_ATTRIBUTES,
                        ItemFlag.HIDE_ENCHANTS
                    )
                    .toButton { _, _ ->
                        if (entry.value.closeInventory)
                        {
                            player.closeInventory()
                        }

                        when (entry.value.action)
                        {
                            UIItemAction.CONSOLE_PERFORM_COMMAND ->
                            {
                                entry.value.actionData.forEach { command ->
                                    Bukkit.dispatchCommand(
                                        Bukkit.getConsoleSender(),
                                        command.replace("{player}", player.name)
                                            .replace("{playerId}", player.uniqueId.toString())
                                    )
                                }
                            }
                            UIItemAction.PERFORM_COMMAND ->
                            {
                                entry.value.actionData.forEach { command ->
                                    Bukkit.dispatchCommand(
                                        player,
                                        command
                                    )
                                }
                            }
                            UIItemAction.SEND_PLAYER_MESSAGE ->
                            {
                                entry.value.actionData.forEach { text ->
                                    player.sendMessage(text)
                                }
                            }
                            UIItemAction.NONE ->
                            {

                            }
                        }
                    }
            }

        override fun size(buttons: Map<Int, Button>) = this@EditableUI.size

        init
        {
            async = true
            autoUpdate = this@EditableUI.autoUpdate
            inventoryType = this@EditableUI.inventoryType

            when (this@EditableUI.style)
            {
                UIStyle.BORDER_FILLED -> placeholdBorders = true
                UIStyle.FILLED -> placeholder = true
                UIStyle.NONE ->
                {

                }
            }
        }
    }
}
