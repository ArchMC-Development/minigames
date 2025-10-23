package gg.tropic.practice.commands.menu.admin.kit.display

import com.cryptomorin.xseries.XMaterial
import gg.tropic.practice.commands.menu.admin.kit.SpecificKitEditorMenu
import gg.tropic.practice.commands.menu.admin.map.SpecificMapEditorMenu
import gg.tropic.practice.kit.Kit
import gg.tropic.practice.kit.KitService
import gg.tropic.practice.kit.administration.specific.SpecificKitAdminMenu
import gg.tropic.practice.map.Map
import gg.tropic.practice.map.MapService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * Class created on 1/17/2025

 * @author Max C.
 * @project esta-practice
 * @website https://solo.to/redis
 */
class EditKitDisplaysMenu(val kit: Kit): Menu()
{
    init
    {
        placeholder = true
    }

    override fun getTitle(player: Player): String
    {
        return "Edit Kit Displays"
    }

    override fun size(buttons: kotlin.collections.Map<Int, Button>): Int
    {
        return 27
    }

    override fun getButtons(player: Player): kotlin.collections.Map<Int, Button>
    {
        return mutableMapOf<Int, Button>().also { mutableMap ->
            mutableMap[4] = ItemBuilder.of(XMaterial.RED_BED)
                .name("${CC.B_RED}Go Back")
                .addToLore(
                    "${CC.GRAY}Go back to the kit editor",
                    "",
                    "${CC.GREEN}Click to go back!"
                ).toButton { _, _ ->
                    SpecificKitEditorMenu(kit).openMenu(player)
                }

            mutableMap[11] = ItemBuilder.of(XMaterial.NAME_TAG)
                .name("${CC.B_GREEN}Change Name")
                .addToLore(
                    "${CC.GRAY}Change the name of this kit",
                    "${CC.GRAY}that is seen when the player is",
                    "${CC.GRAY}selecting said kit.",
                    "",
                    "${CC.GREEN}Click to change!"
                ).toButton { _, _ ->
                    InputPrompt()
                        .withText("${CC.GREEN}Enter the new name that you want this kit to have:")
                        .acceptInput { _, input ->
                            kit.displayName = input

                            with (KitService.cached())
                            {
                                this.kits[kit.id] = kit
                                KitService.sync(this)
                            }

                            EditKitDisplaysMenu(kit).openMenu(player)
                        }.start(player)
                }

            mutableMap[13] = ItemBuilder.of(XMaterial.PAPER)
                .name("${CC.B_GREEN}Change Description")
                .addToLore(
                    "${CC.GRAY}Change the description of this kit",
                    "${CC.GRAY}that is seen when the player is",
                    "${CC.GRAY}choosing the kit or viewing areas",
                    "${CC.GRAY}with the kit in it.",
                    "",
                    "${CC.GREEN}Click to change!"
                ).toButton { _, _ ->
                    InputPrompt()
                        .withText("${CC.GREEN}Enter the new description that you want this kit to have:")
                        .acceptInput { _, input ->
                            kit.description = input

                            with (KitService.cached())
                            {
                                this.kits[kit.id] = kit
                                KitService.sync(this)
                            }

                            EditKitDisplaysMenu(kit).openMenu(player)
                        }.start(player)
                }

            mutableMap[15] = ItemBuilder.of(XMaterial.STICK)
                .name("${CC.B_GREEN}Change Icon")
                .addToLore(
                    "${CC.GRAY}Change the icon of this kit",
                    "${CC.GRAY}that is seen when the player is",
                    "${CC.GRAY}selecting said kit.",
                    "",
                    "${CC.GREEN}Click to change!"
                ).toButton { _, _ ->
                    InputPrompt()
                        .withText("${CC.GREEN}Enter the new item that you want this kit's icon to be:")
                        .acceptInput { _, input ->
                            val item = Material.values().firstOrNull { it.name.equals(input, ignoreCase = true)}

                            if (item == null)
                            {
                                player.sendMessage("${CC.RED}This is an invalid material.")
                                return@acceptInput
                            }

                            kit.displayIcon = ItemBuilder.of(item).build()

                            with (KitService.cached())
                            {
                                this.kits[kit.id] = kit
                                KitService.sync(this)
                            }

                            EditKitDisplaysMenu(kit).openMenu(player)
                        }.start(player)
                }
        }
    }
}