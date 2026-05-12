package mc.arch.minigames.persistent.housing.game.menu.house.player

import com.cryptomorin.xseries.XMaterial
import mc.arch.minigames.persistent.housing.api.model.PlayerHouse
import mc.arch.minigames.persistent.housing.game.getReference
import mc.arch.minigames.persistent.housing.game.menu.house.MainHouseMenu
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.menus.ConfirmMenu
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import kotlin.math.roundToInt

class PlayerManagementMenu(val house: PlayerHouse) : PaginatedMenu()
{
    init
    {
        placeholdBorders = true
        updateAfterClick = true
    }

    override fun size(buttons: Map<Int, Button>) = 36
    override fun getMaxItemsPerPage(player: Player) = 14
    override fun getAllPagesButtonSlots() = (10..16).toList() + (19..25).toList()

    override fun getPrePaginatedTitle(player: Player): String = "Managing Players"

    override fun getGlobalButtons(player: Player): Map<Int, Button> = mutableMapOf(
        31 to MainHouseMenu.mainMenuButton(house)
    )

    override fun getAllPagesButtons(player: Player): Map<Int, Button> = mutableMapOf<Int, Button>().also { buttons ->
        val onlinePlayers = house.getReference()?.onlinePlayers
            ?.mapNotNull { Bukkit.getPlayer(it) } ?: listOf()

        val viewerCanManage = house.playerIsOrAboveAdministrator(player.uniqueId)
        val viewerCanAssignRoles = viewerCanManage || house.hasPermission(player.uniqueId, "house.manage")

        onlinePlayers.forEach { other ->
            val role = house.getRole(other.uniqueId)
            val location = other.location

            val health = other.health
            val maxHealth = other.maxHealth
            val healthPercentage = ((health / maxHealth) * 100).roundToInt()

            val isOwnerTarget = house.owner == other.uniqueId
            val targetingSelf = other.uniqueId == player.uniqueId
            val canKickThisPlayer = viewerCanManage && !targetingSelf && !isOwnerTarget

            buttons[buttons.size] = ItemBuilder.of(XMaterial.PLAYER_HEAD)
                .name("${CC.GREEN}${other.name}")
                .addToLore(
                    "${CC.YELLOW}Role: ${role.coloredName()}",
                    "${CC.YELLOW}Health: ${CC.WHITE}$healthPercentage% ${CC.GRAY}(${health.roundToInt()}/${maxHealth.roundToInt()})",
                    "${CC.YELLOW}Location: ${CC.WHITE}${location.blockX}, ${location.blockY}, ${location.blockZ}",
                    ""
                ).also { button ->
                    if (viewerCanAssignRoles)
                    {
                        button.addToLore("${CC.GREEN}Left-Click to manage roles")
                    }

                    if (canKickThisPlayer)
                    {
                        button.addToLore(
                            "${CC.YELLOW}Middle-Click to kick player",
                            "${CC.RED}Right-Click to ban player"
                        )
                    }
                }
                .toButton { _, click ->
                    val clickType = click ?: return@toButton

                    when (clickType)
                    {
                        ClickType.LEFT, ClickType.SHIFT_LEFT, ClickType.DOUBLE_CLICK ->
                        {
                            if (viewerCanAssignRoles)
                            {
                                PlayerRoleAssignMenu(house, other.uniqueId).openMenu(player)
                            } else
                            {
                                player.sendMessage("${CC.RED}You do not have permission to manage roles!")
                            }
                        }

                        ClickType.MIDDLE, ClickType.CREATIVE ->
                        {
                            if (!canKickThisPlayer)
                            {
                                if (targetingSelf)
                                {
                                    player.sendMessage("${CC.RED}You cannot kick yourself!")
                                } else if (isOwnerTarget)
                                {
                                    player.sendMessage("${CC.RED}You cannot kick the realm owner!")
                                } else
                                {
                                    player.sendMessage("${CC.RED}You do not have permission to kick players!")
                                }
                                return@toButton
                            }

                            ConfirmMenu("Kick ${other.name}?") { confirmed ->
                                if (confirmed)
                                {
                                    other.kickPlayer("${CC.RED}You have been kicked from this realm!")
                                    player.sendMessage("${CC.GREEN}Successfully kicked ${other.name}!")
                                }

                                PlayerManagementMenu(house).openMenu(player)
                            }.openMenu(player)
                        }

                        ClickType.RIGHT, ClickType.SHIFT_RIGHT ->
                        {
                            if (!canKickThisPlayer)
                            {
                                if (targetingSelf)
                                {
                                    player.sendMessage("${CC.RED}You cannot ban yourself!")
                                } else if (isOwnerTarget)
                                {
                                    player.sendMessage("${CC.RED}You cannot ban the realm owner!")
                                } else
                                {
                                    player.sendMessage("${CC.RED}You do not have permission to ban players!")
                                }
                                return@toButton
                            }

                            ConfirmMenu("Ban ${other.name}?") { confirmed ->
                                if (confirmed)
                                {
                                    if (!house.housingBans.contains(other.uniqueId))
                                    {
                                        house.housingBans.add(other.uniqueId)
                                        house.save()
                                    }

                                    other.kickPlayer("${CC.RED}You have been banned from this realm!")
                                    player.sendMessage("${CC.B_GREEN}SUCCESS! ${CC.GREEN}You have banned ${other.name} from your realm!")
                                }

                                PlayerManagementMenu(house).openMenu(player)
                            }.openMenu(player)
                        }

                        else -> { }
                    }
                }
        }
    }
}
