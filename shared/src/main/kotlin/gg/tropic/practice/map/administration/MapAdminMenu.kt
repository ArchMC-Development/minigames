package gg.tropic.practice.map.administration

import gg.tropic.practice.map.Map
import gg.tropic.practice.kit.administration.specific.SpecificKitAdminMenu
import gg.tropic.practice.map.administration.specific.SpecificMapAdminMenu
import gg.tropic.practice.menu.TemplateMapMenu
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

/**
 * @author GrowlyX
 * @since 7/28/2024
 */
class MapAdminMenu : TemplateMapMenu()
{
    override fun filterDisplayOfMap(map: Map) = true
    override fun itemDescriptionOf(player: Player, map: Map) = listOf(
        "${CC.GRAY}ID: ${CC.WHITE}${map.name}",
        "${CC.GRAY}Display: ${CC.WHITE}${map.displayName}",
        "${CC.GRAY}Slime: ${CC.WHITE}${map.associatedSlimeTemplate}",
        "",
        "${CC.YELLOW}Metadata: ${CC.WHITE}",
        *map.metadata.metadata
            .map { "${CC.WHITE}- $it" }
            .toTypedArray(),
        "",
        "${CC.YELLOW}Groups:",
        *map.associatedKitGroups
            .map { "${CC.WHITE}- $it" }
            .toTypedArray(),
        "",
        "${CC.GRAY}Locked: ${
            if (map.locked) "${CC.D_GREEN}✔" else "${CC.D_RED}✘"
        }",
        "",
        "${CC.GREEN}Click to manage!"
    )

    override fun itemClicked(player: Player, map: Map, type: ClickType)
    {
        Button.playNeutral(player)
        SpecificMapAdminMenu(map).openMenu(player)
    }

    override fun getPrePaginatedTitle(player: Player) = "Map Administration"
}
