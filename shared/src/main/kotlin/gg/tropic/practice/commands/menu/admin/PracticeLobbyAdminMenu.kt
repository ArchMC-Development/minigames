package gg.tropic.practice.commands.menu.admin

import com.cryptomorin.xseries.XMaterial
import gg.tropic.practice.commands.menu.admin.config.PracticeConfigurationMenu
import gg.tropic.practice.commands.menu.admin.kit.KitEditorSelectionMenu
import gg.tropic.practice.commands.menu.admin.map.MapEditorSelectionMenu
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player

class PracticeLobbyAdminMenu: Menu()
{
    init
    {
        placeholder = true
    }

    override fun getButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf<Int, Button>().also {
            it[10] = ItemBuilder.of(XMaterial.DIAMOND_SWORD)
                .name("${CC.B_GREEN}Edit Kits")
                .addToLore(
                    "${CC.GRAY}Edit all of the kits that",
                    "${CC.GRAY}are currently on the server.",
                    "",
                    "${CC.GREEN}Click to edit!"
                ).toButton { _, _ ->
                    KitEditorSelectionMenu(player).openMenu(player)
                }

            it[12] = ItemBuilder.of(XMaterial.MAP)
                .name("${CC.B_YELLOW}Edit Maps")
                .addToLore(
                    "${CC.GRAY}Edit all of the maps that",
                    "${CC.GRAY}are currently on the server.",
                    "",
                    "${CC.GREEN}Click to edit!"
                ).toButton { _, _ ->
                    MapEditorSelectionMenu().openMenu(player)
                }

            it[14] = ItemBuilder.of(XMaterial.COMPASS)
                .name("${CC.B_RED}Practice Settings")
                .addToLore(
                    "${CC.GRAY}Edit all of the practice-specific",
                    "${CC.GRAY}settings that are currently",
                    "${CC.GRAY}available.",
                    "",
                    "${CC.GREEN}Click to configure!"
                ).toButton { _, _ ->
                    PracticeConfigurationMenu().openMenu(player)
                }
        }
    }

    override fun size(buttons: Map<Int, Button>): Int = 27
    override fun getTitle(player: Player): String = "Select an Option"
}
