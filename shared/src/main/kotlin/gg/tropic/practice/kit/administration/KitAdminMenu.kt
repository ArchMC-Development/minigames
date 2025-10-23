package gg.tropic.practice.kit.administration

import gg.tropic.practice.kit.Kit
import gg.tropic.practice.kit.administration.specific.SpecificKitAdminMenu
import gg.tropic.practice.menu.TemplateKitMenu
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

/**
 * @author GrowlyX
 * @since 7/28/2024
 */
class KitAdminMenu(player: Player) : TemplateKitMenu(player)
{
    override fun filterDisplayOfKit(player: Player, kit: Kit) = true
    override fun itemDescriptionOf(player: Player, kit: Kit) = listOf(
        "${CC.GRAY}ID: ${CC.WHITE}${kit.id}",
        "${CC.GRAY}Display: ${CC.WHITE}${kit.displayName}",
        "${CC.GRAY}Lifecycle: ${CC.WHITE}${kit.lifecycleType ?: "*SoulBound"}",
        "",
        "${CC.GRAY}Locked: ${
            if (!kit.enabled) "${CC.D_GREEN}✔" else "${CC.D_RED}✘"
        }",
        "",
        "${CC.GREEN}Click to manage!"
    )

    override fun itemClicked(player: Player, kit: Kit, type: ClickType)
    {
        Button.playNeutral(player)
        SpecificKitAdminMenu(kit).openMenu(player)
    }

    override fun getPrePaginatedTitle(player: Player) = "Kit Administration"
}
