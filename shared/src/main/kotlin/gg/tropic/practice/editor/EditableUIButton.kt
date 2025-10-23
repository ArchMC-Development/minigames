package gg.tropic.practice.editor

import com.cryptomorin.xseries.XMaterial
import net.evilblock.cubed.util.CC
import org.bukkit.inventory.ItemStack

/**
 * @author Subham
 * @since 7/5/25
 */
data class EditableUIButton(
    var displayName: String = "${CC.B_GREEN}TEST UI ITEM",
    var icon: ItemStack = XMaterial.COMPASS.parseItem()!!,
    var amount: Int = 1,
    var closeInventory: Boolean = false,
    var lore: List<String> = listOf("${CC.GRAY}Example lore"),
    var action: UIItemAction = UIItemAction.SEND_PLAYER_MESSAGE,
    var actionData: List<String> = listOf("${CC.RED}This is a test UI item!")
)
