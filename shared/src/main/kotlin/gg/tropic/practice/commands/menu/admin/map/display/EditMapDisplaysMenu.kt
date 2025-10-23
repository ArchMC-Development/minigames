package gg.tropic.practice.commands.menu.admin.map.display

import com.cryptomorin.xseries.XMaterial
import gg.tropic.practice.commands.menu.admin.map.MapEditorSelectionMenu
import gg.tropic.practice.commands.menu.admin.map.SpecificMapEditorMenu
import gg.tropic.practice.map.Map
import gg.tropic.practice.map.MapService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * Class created on 1/17/2025

 * @author Max C.
 * @project esta-practice
 * @website https://solo.to/redis
 */
class EditMapDisplaysMenu(val map: Map): Menu()
{
    init
    {
        placeholder = true
    }

    override fun getTitle(player: Player): String
    {
        return "Edit Map Displays"
    }

    override fun size(buttons: kotlin.collections.Map<Int, Button>): Int
    {
        return 9
    }

    override fun getButtons(player: Player): kotlin.collections.Map<Int, Button>
    {
        return mutableMapOf<Int, Button>().also { mutableMap ->
            mutableMap[2] = ItemBuilder.of(XMaterial.NAME_TAG)
                .name("${CC.B_GREEN}Change Name")
                .addToLore(
                    "${CC.GRAY}Change the name of this map",
                    "${CC.GRAY}that is seen when the player is",
                    "${CC.GRAY}teleported to the map or selects the map.",
                    "",
                    "${CC.GREEN}Click to change!"
                ).toButton { _, _ ->
                    InputPrompt()
                        .withText("${CC.GREEN}Enter the new name that you want this map to have:")
                        .acceptInput { _, input ->
                            map.displayName = input

                            with (MapService.cached())
                            {
                                this.maps[map.name] = map
                                MapService.sync(this)
                            }

                            EditMapDisplaysMenu(map).openMenu(player)
                        }.start(player)
                }

            mutableMap[4] = ItemBuilder.of(XMaterial.RED_BED)
                .name("${CC.B_RED}Go Back")
                .addToLore(
                    "${CC.GRAY}Go back to the map editor",
                    "",
                    "${CC.GREEN}Click to go back!"
                ).toButton { _, _ ->
                    SpecificMapEditorMenu(map).openMenu(player)
                }

            mutableMap[6] = ItemBuilder.of(XMaterial.STICK)
                .name("${CC.B_GREEN}Change Icon")
                .addToLore(
                    "${CC.GRAY}Change the icon of this map",
                    "${CC.GRAY}that is seen when the player is",
                    "${CC.GRAY}selecting the map.",
                    "",
                    "${CC.GREEN}Click to change!"
                ).toButton { _, _ ->
                    InputPrompt()
                        .withText("${CC.GREEN}Enter the new item that you want this map's icon to be:")
                        .acceptInput { _, input ->
                            val item = Material.values().firstOrNull { it.name.equals(input, ignoreCase = true)}

                            if (item == null)
                            {
                                player.sendMessage("${CC.RED}This is an invalid material.")
                                return@acceptInput
                            }

                            map.displayIcon = ItemBuilder.of(item).build()

                            with (MapService.cached())
                            {
                                this.maps[map.name] = map
                                MapService.sync(this)
                            }

                            EditMapDisplaysMenu(map).openMenu(player)
                        }.start(player)
                }
        }
    }
}