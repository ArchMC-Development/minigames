package gg.tropic.practice.commands.menu.admin.kit.groups

import com.cryptomorin.xseries.XMaterial
import gg.tropic.practice.commands.menu.admin.kit.SpecificKitEditorMenu
import gg.tropic.practice.kit.Kit
import gg.tropic.practice.kit.group.KitGroupService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player

/**
 * Class created on 4/6/2025

 * @author Max C.
 * @project arch-duels
 * @website https://solo.to/redis
 */
class KitGroupEditorMenu(val kit: Kit): PaginatedMenu()
{
    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf<Int, Button>().also { map ->
            KitGroupService.cached().groups.forEach { group ->
                map[map.size] = ItemBuilder.of(if (group.contains.contains(kit.id)) XMaterial.GREEN_WOOL else XMaterial.RED_WOOL)
                    .name("${CC.B_YELLOW}${group.id}")
                    .setLore(mutableListOf("${CC.GRAY}Contains:") + group.contains.map { "${CC.GRAY}- ${CC.WHITE}$it" })
                    .toButton { _, _ ->
                        if (group.contains.contains(kit.id))
                        {
                            group.contains.remove(kit.id)
                        } else
                        {
                            group.contains.add(kit.id)
                        }

                        KitGroupService.sync(KitGroupService.cached())
                    }
            }
        }
    }

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose)
        {
            SpecificKitEditorMenu(kit).openMenu(player)
        }
    }

    override fun getPrePaginatedTitle(player: Player): String
    {
        return "Select a Group..."
    }
}