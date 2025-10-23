package gg.tropic.practice.commands.menu.admin.kit

import gg.tropic.practice.kit.Kit
import gg.tropic.practice.kit.administration.specific.SpecificKitAdminMenu
import gg.tropic.practice.menu.TemplateKitMenu
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

class KitEditorSelectionMenu(player: Player) : TemplateKitMenu(player)
{
    override fun filterDisplayOfKit(player: Player, kit: Kit) = true
    override fun itemDescriptionOf(player: Player, kit: Kit) = listOf(
        if (kit.enabled) "${CC.GREEN}Currently Enabled..." else "${CC.RED}Currently Disabled...",
        "",
        "${CC.GREEN}Display Information:",
        "${CC.GRAY}Identifier: ${CC.WHITE}${kit.id}",
        "${CC.GRAY}Display Name: ${CC.WHITE}${kit.displayName}",
        "${CC.GRAY}Lifecycle Type: ${CC.WHITE}${kit.lifecycleType ?: "SoulBound"}",
        "",
        "${CC.GREEN}Content Information:",
        "${CC.GRAY}Inventory Contents: ${CC.WHITE}${kit.contents.count { it != null }}",
        "${CC.GRAY}Armor Contents: ${CC.WHITE}${kit.armorContents.count { it != null }}",
        "${CC.GRAY}Additional Contents: ${CC.WHITE}${kit.additionalContents.count { it != null }}",
        "",
        "${CC.GREEN}Click to manage this kit!"
    )

    override fun itemClicked(player: Player, kit: Kit, type: ClickType)
    {
        Button.playNeutral(player)
        SpecificKitEditorMenu(kit).openMenu(player)
    }

    override fun getPrePaginatedTitle(player: Player) = "Kit Administration"
}
