package mc.arch.minigames.persistent.housing.game.menu.house.roles

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.util.CallbackInputPrompt
import mc.arch.minigames.persistent.housing.api.model.PlayerHouse
import mc.arch.minigames.persistent.housing.api.role.HouseRole
import mc.arch.minigames.persistent.housing.game.menu.house.MainHouseMenu
import mc.arch.minigames.persistent.housing.game.translateCC
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player

class RoleEditorMenu(val house: PlayerHouse) : PaginatedMenu()
{
    init
    {
        placeholdBorders = true
    }

    override fun size(buttons: Map<Int, Button>) = 27
    override fun getMaxItemsPerPage(player: Player) = 9
    override fun getAllPagesButtonSlots() = (10..18).toList()

    override fun getPrePaginatedTitle(player: Player): String = "Viewing All Roles"

    override fun getGlobalButtons(player: Player): Map<Int, Button> = mutableMapOf(
        4 to ItemBuilder.of(XMaterial.EMERALD)
            .name("${CC.GREEN}Create Role")
            .addToLore("${CC.YELLOW}Click to create a new Role!")
            .toButton { _, _ ->
                CallbackInputPrompt("${CC.GREEN}Please type in the name you want this Role to have:") { input ->
                    if (house.roles.containsKey(input.lowercase())) {
                        player.sendMessage("${CC.RED}A role with that name already exists!")
                        return@CallbackInputPrompt
                    }

                    val role = HouseRole(input)
                    house.roles[role.id] = role
                    house.save()

                    player.sendMessage("${CC.B_GREEN}SUCCESS! ${CC.GREEN}You have created a role!")
                    RoleEditorMenu(house).openMenu(player)
                }.start(player)
            },
        22 to MainHouseMenu.mainMenuButton(house)
    )

    override fun getAllPagesButtons(player: Player): Map<Int, Button> = mutableMapOf<Int, Button>().also { buttons ->
        house.roles.values.forEach { role ->
            buttons[buttons.size] = ItemBuilder.of(getWoolForColor(role.color))
                .name("${role.color}${role.displayName}")
                .addToLore(
                    "${CC.YELLOW}Prefix: ${CC.WHITE}${role.prefix.translateCC()}",
                    "${CC.YELLOW}Permissions: ${CC.WHITE}${role.permissions.size}",
                    "${CC.YELLOW}Default: ${CC.WHITE}${if (role.default) "${CC.GREEN}Yes" else "${CC.RED}No"}",
                    "",
                    "${CC.GREEN}Left-Click to edit role",
                    "${CC.RED}Right-Click to delete role"
                )
                .toButton { _, click ->
                    if (click!!.isLeftClick) {
                        RoleSpecificsEditorMenu(house, role).openMenu(player)
                    } else if (click.isRightClick) {
                         if (role.default || role.id == "owner") {
                             player.sendMessage("${CC.RED}This role is unable to be deleted!")
                             return@toButton
                         }

                         house.roles.remove(role.id)
                         house.save()

                         player.sendMessage("${CC.RED}Deleted Role!")
                         RoleEditorMenu(house).openMenu(player)
                    }
                }
        }
    }

    fun getWoolForColor(colorCode: String): XMaterial {
        return when (colorCode.replace("&", "")) {
            "0" -> XMaterial.BLACK_WOOL
            "1" -> XMaterial.BLUE_WOOL
            "2" -> XMaterial.GREEN_WOOL
            "3" -> XMaterial.CYAN_WOOL
            "4" -> XMaterial.RED_WOOL
            "5" -> XMaterial.PURPLE_WOOL
            "6" -> XMaterial.ORANGE_WOOL
            "7" -> XMaterial.LIGHT_GRAY_WOOL
            "8" -> XMaterial.GRAY_WOOL
            "9" -> XMaterial.BLUE_WOOL
            "a" -> XMaterial.LIME_WOOL
            "b" -> XMaterial.LIGHT_BLUE_WOOL
            "c" -> XMaterial.RED_WOOL
            "d" -> XMaterial.PINK_WOOL
            "e" -> XMaterial.YELLOW_WOOL
            "f" -> XMaterial.WHITE_WOOL
            else -> XMaterial.WHITE_WOOL
        }
    }
}