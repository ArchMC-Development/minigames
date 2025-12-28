package mc.arch.minigames.persistent.housing.game.menu.house.roles

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.util.CallbackInputPrompt
import mc.arch.minigames.persistent.housing.api.model.PlayerHouse
import mc.arch.minigames.persistent.housing.api.role.HouseRole
import mc.arch.minigames.persistent.housing.game.translateCC
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player

class RoleSpecificsEditorMenu(val house: PlayerHouse, val role: HouseRole) : Menu("Editing Role...")
{
    init
    {
        placeholder = true
        updateAfterClick = true
    }

    override fun size(buttons: Map<Int, Button>): Int = 27

    override fun getButtons(player: Player): Map<Int, Button> = mutableMapOf(
        10 to ItemBuilder.of(XMaterial.NAME_TAG)
            .name("${CC.GREEN}Edit Display Name")
            .addToLore(
                "${CC.YELLOW}Current: ${CC.WHITE}${role.displayName}",
                "",
                "${CC.GREEN}Click to edit display name"
            )
            .toButton { _, _ ->
                CallbackInputPrompt("${CC.GREEN}Type the new display name:") { input ->
                    role.displayName = input
                    house.roles[role.id] = role
                    house.save()

                    player.sendMessage("${CC.GREEN}Updated display name!")
                    RoleSpecificsEditorMenu(house, role).openMenu(player)
                }.start(player)
            },
        11 to ItemBuilder.of(XMaterial.WHITE_WOOL)
            .name("${CC.GREEN}Edit Color")
            .addToLore(
                "${CC.YELLOW}Current: ${role.color.translateCC()}This",
                "",
                "${CC.GREEN}Click to edit color code"
            )
            .toButton { _, _ ->
                CallbackInputPrompt("${CC.GREEN}Type the new color code in chat:") { input ->
                    if (!input.startsWith("&"))
                    {
                        player.sendMessage("${CC.RED}This is an invalid color code!")
                        return@CallbackInputPrompt
                    }

                    role.color = input
                    house.roles[role.id] = role
                    house.save()

                    player.sendMessage("${CC.GREEN}Updated color!")
                    RoleSpecificsEditorMenu(house, role).openMenu(player)
                }.start(player)
            },
        12 to ItemBuilder.of(XMaterial.OAK_SIGN)
            .name("${CC.GREEN}Edit Prefix")
            .addToLore(
                "${CC.YELLOW}Current: ${CC.WHITE}${role.prefix}",
                "",
                "${CC.GREEN}Click to edit prefix"
            )
            .toButton { _, _ ->
                CallbackInputPrompt("${CC.GREEN}Type the new prefix:") { input ->
                    role.prefix = input
                    house.roles[role.id] = role
                    house.save()

                    player.sendMessage("${CC.GREEN}Updated prefix!")
                    RoleSpecificsEditorMenu(house, role).openMenu(player)
                }.start(player)
            },
        13 to ItemBuilder.of(XMaterial.PAPER)
            .name("${CC.GREEN}Edit Permissions")
            .addToLore(
                "${CC.YELLOW}Current Permissions: ${CC.WHITE}${role.permissions.size}",
                "",
                "${CC.GREEN}Click to edit permissions"
            )
            .toButton { _, _ ->

            },
        15 to ItemBuilder.of(XMaterial.ARROW)
            .name("${CC.GREEN}Go Back")
            .addToLore("${CC.YELLOW}Click to go back")
            .toButton { _, _ ->
                RoleEditorMenu(house).openMenu(player)
            },
        16 to ItemBuilder.of(XMaterial.RED_WOOL)
            .name("${CC.RED}Delete Role")
            .addToLore("${CC.YELLOW}Click to permanently remove")
            .toButton { _, _ ->
                if (role.default || role.name == "owner")
                {
                    player.sendMessage("${CC.RED}You cannot delete this role!")
                    return@toButton
                }

                house.roles.remove(role.id)
                house.save()

                player.sendMessage("${CC.RED}Deleted Role!")
                RoleEditorMenu(house).openMenu(player)
            }
    )
}
