package mc.arch.minigames.persistent.housing.game.menu.house.player

import com.cryptomorin.xseries.XMaterial
import mc.arch.minigames.persistent.housing.api.model.PlayerHouse
import mc.arch.minigames.persistent.housing.game.menu.house.roles.RoleEditorMenu
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

class PlayerRoleAssignMenu(val house: PlayerHouse, val targetUuid: UUID) : PaginatedMenu() {
    init {
        placeholdBorders = true
    }

    override fun size(buttons: Map<Int, Button>) = 36
    override fun getMaxItemsPerPage(player: Player) = 14
    override fun getAllPagesButtonSlots() = (10..16).toList() + (19..25).toList()

    override fun getPrePaginatedTitle(player: Player): String {
        val targetName = Bukkit.getPlayer(targetUuid)?.name ?: "Player"
        return "Assign Role to $targetName"
    }

    override fun getGlobalButtons(player: Player): Map<Int, Button> = mutableMapOf(
        31 to ItemBuilder.of(XMaterial.ARROW).name("${CC.RED}Back to Management").toButton { _, _ ->
            PlayerManagementMenu(house).openMenu(player)
        }
    )

    override fun getAllPagesButtons(player: Player): Map<Int, Button> = mutableMapOf<Int, Button>().also { buttons ->
        val currentRole = house.getRole(targetUuid)

        house.roles.values.forEach { role ->
            buttons[buttons.size] = ItemBuilder.of(RoleEditorMenu.getWoolForColor(role.color))
                .name(role.coloredName())
                .addToLore(
                    "",
                    if (currentRole.id == role.id) "${CC.GREEN}They already have this role!" else "${CC.GREEN}Click to assign this role!"
                )
                .toButton { _, _ ->
                    if (targetUuid == house.owner && player.uniqueId != house.owner) {
                        player.sendMessage("${CC.RED}You cannot modify the role of the realm owner.")
                        PlayerManagementMenu(house).openMenu(player)
                        return@toButton
                    }

                    if (currentRole.id != role.id) {
                        if (role.default) {
                            house.playerRoles.remove(targetUuid)
                        } else {
                            house.playerRoles[targetUuid] = role.id
                        }

                        house.save()

                        Bukkit.getPlayer(targetUuid)?.let { target ->
                            NametagHandler.reloadPlayer(target)
                        }

                        player.sendMessage("${CC.GREEN}Successfully assigned ${role.name} ${CC.GREEN}to the player!")
                    }

                    PlayerManagementMenu(house).openMenu(player)
                }
        }
    }
}
