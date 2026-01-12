package mc.arch.minigames.persistent.housing.game.menu.house.roles

import com.cryptomorin.xseries.XMaterial
import mc.arch.minigames.persistent.housing.api.model.PlayerHouse
import mc.arch.minigames.persistent.housing.api.role.HousePermission
import mc.arch.minigames.persistent.housing.api.role.HouseRole
import mc.arch.minigames.persistent.housing.game.menu.house.MainHouseMenu
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player
import kotlin.collections.set

/**
 * Class created on 1/11/2026

 * @author Max C.
 * @project arch-minigames
 * @website https://solo.to/redis
 */
class RolePermissionSelectMenu(val house: PlayerHouse, val role: HouseRole) : PaginatedMenu()
{
    init
    {
        placeholdBorders = true
    }

    override fun size(buttons: Map<Int, Button>) = 27
    override fun getMaxItemsPerPage(player: Player) = 9
    override fun getAllPagesButtonSlots() = (10..18).toList()

    override fun getPrePaginatedTitle(player: Player): String = "Select a Permission"

    override fun getGlobalButtons(player: Player): Map<Int, Button> = mutableMapOf(
        22 to MainHouseMenu.mainMenuButton(house)
    )

    override fun getAllPagesButtons(player: Player): Map<Int, Button> = mutableMapOf<Int, Button>().also { buttons ->
        HousePermission.entries
            .forEach { permission ->
                buttons[buttons.size] = ItemBuilder.of(XMaterial.NETHER_STAR)
                    .name("${CC.GREEN}${permission.displayName}")
                    .also { button ->
                        permission.description.forEach {
                            button.addToLore("${CC.GRAY}${it}")
                        }
                    }
                    .addToLore(
                        "",
                        "${CC.RED}Left-Click to select!",
                    )
                    .toButton { _, click ->
                        if (click!!.isLeftClick)
                        {
                            role.permissions.add(permission.node)
                            house.roles[role.id] = role
                            house.save()

                            Button.playNeutral(player)
                            RolePermissionEditorMenu(house, role).openMenu(player)
                        }
                    }
            }
    }
}