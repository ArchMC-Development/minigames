package gg.tropic.practice.commands.menu.admin.map

import com.cryptomorin.xseries.XMaterial
import gg.tropic.practice.commands.menu.admin.map.display.EditMapDisplaysMenu
import gg.tropic.practice.commands.menu.rating.MapRatingOverviewMenu
import gg.tropic.practice.map.Map
import gg.tropic.practice.map.MapService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player

class SpecificMapEditorMenu(val map: Map): Menu()
{
    init
    {
        placeholder = true
    }

    override fun size(buttons: kotlin.collections.Map<Int, Button>): Int
    {
        return 36
    }

    override fun getTitle(player: Player): String
    {
        return "Editing: ${map.displayName}"
    }

    override fun getButtons(player: Player): kotlin.collections.Map<Int, Button>
    {
        return mutableMapOf<Int, Button>().also {
            it[10] = ItemBuilder.of(XMaterial.GREEN_WOOL)
                .name("${CC.B_GREEN}View Map Ratings")
                .addToLore(
                    "${CC.GRAY}Interested to see how this map",
                    "${CC.GRAY}is performing among your players?",
                    "${CC.GRAY}Check out the map ratings to get",
                    "${CC.GRAY}in-depth map analysis.",
                    "",
                    "${CC.GREEN}Click to view!"
                ).toButton { _, _ ->
                    MapRatingOverviewMenu().openMenu(player)
                }

            it[11] = ItemBuilder.of(XMaterial.RED_WOOL)
                .name("${CC.B_RED}Delete Map")
                .addToLore(
                    "${CC.GRAY}Delete this map from the server",
                    "${CC.GRAY}and remove all copies from use.",
                    "",
                    "${CC.GREEN}Click to delete!"
                ).toButton { _, _ ->
                    with (MapService.cached()) {
                        this.maps.remove(map.name)
                        MapService.sync(this)
                    }

                    player.sendMessage("${CC.RED}You have deleted this map from the server!")
                    MapEditorSelectionMenu().openMenu(player)
                }

            it[13] = ItemBuilder.copyOf(map.displayIcon)
                .name("${CC.B_YELLOW}${map.displayName}")
                .addToLore(
                    "${CC.GRAY}You are currently in the management menu",
                    "${CC.GRAY}for the map ${CC.WHITE}${map.displayName}${CC.GRAY}.",
                ).toButton()

            it[14] = ItemBuilder.of(XMaterial.RED_BED)
                .name("${CC.B_RED}Go Back")
                .addToLore(
                    "${CC.GRAY}Go back to the map editor",
                    "${CC.GRAY}selection menu.",
                    "",
                    "${CC.GREEN}Click to go back!"
                ).toButton { _, _ ->
                    MapEditorSelectionMenu().openMenu(player)
                }

            it[16] = ItemBuilder.of(XMaterial.FIRE_CHARGE)
                .name("${CC.B_YELLOW}Edit Current Groups")
                .addToLore(
                    "${CC.GRAY}Edit the specific groups that",
                    "${CC.GRAY}this map tailors to.",
                    "",
                    "${CC.GREEN}Click to edit!"
                ).toButton { _, _ ->

                }

            it[22] = ItemBuilder.of(XMaterial.NAME_TAG)
                .name("${CC.BD_PURPLE}Edit Display Properties")
                .addToLore(
                    "${CC.GRAY}Edit the display properties of",
                    "${CC.GRAY}this map. This includes traits",
                    "${CC.GRAY}such as display name, description,",
                    "${CC.GRAY}and icon.",
                    "",
                    "${CC.GREEN}Click to edit!"
                ).toButton { _, _ ->
                    EditMapDisplaysMenu(map).openMenu(player)
                }
        }
    }
}