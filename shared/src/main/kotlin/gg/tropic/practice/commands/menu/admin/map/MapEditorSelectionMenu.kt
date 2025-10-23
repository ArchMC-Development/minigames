package gg.tropic.practice.commands.menu.admin.map

import gg.tropic.practice.map.Map
import gg.tropic.practice.menu.TemplateMapMenu
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

class MapEditorSelectionMenu : TemplateMapMenu()
{
    override fun filterDisplayOfMap(map: Map) = true
    override fun itemDescriptionOf(player: Player, map: Map) = listOf(
        if (map.locked) "${CC.RED}Map is locked..." else "${CC.GREEN}Map is unlocked...",
        " ",
        "${CC.GRAY}Identifier: ${CC.WHITE}${map.name}",
        "${CC.GRAY}Display: ${CC.WHITE}${map.displayName}",
        "${CC.GRAY}Template: ${CC.WHITE}${map.associatedSlimeTemplate}",
        "",
        "${CC.YELLOW}Metadata: ${CC.WHITE}",
        *map.metadata.metadata.map {
            "${CC.GRAY}- ${CC.WHITE}${it.id}${it.report()}"
        }.toTypedArray(),
        "",
        "${CC.YELLOW}Groups:",
        *map.associatedKitGroups
            .map { "${CC.WHITE}- $it" }
            .toTypedArray(),
        "",
        "",
        "${CC.GREEN}Click to manage!"
    )

    override fun itemClicked(player: Player, map: Map, type: ClickType)
    {
        Button.playNeutral(player)
        SpecificMapEditorMenu(map).openMenu(player)
    }

    override fun getPrePaginatedTitle(player: Player) = "Map Administration"
}
