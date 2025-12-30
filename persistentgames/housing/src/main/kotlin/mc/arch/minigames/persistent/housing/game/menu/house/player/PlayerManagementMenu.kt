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
import kotlin.math.roundToInt

/**
 * Class created on 12/29/2025
 *
 * @author Max C.
 * @project arch-minigames
 * @website https://solo.to/redis
 */
class PlayerManagementMenu(val house: PlayerHouse) : PaginatedMenu()
{
    init
    {
        placeholdBorders = true
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

        onlinePlayers.forEach { other ->
            val role = house.getRole(other.uniqueId)
            val location = other.location

            val health = other.health
            val maxHealth = other.maxHealth
            val healthPercentage = ((health / maxHealth) * 100).roundToInt()

            buttons[buttons.size] = ItemBuilder.of(XMaterial.PLAYER_HEAD)
                .name("${CC.GREEN}${other.name}")
                .addToLore(
                    "${CC.YELLOW}Role: ${role.coloredName()}",
                    "${CC.YELLOW}Health: ${CC.WHITE}$healthPercentage% ${CC.GRAY}(${health.roundToInt()}/${maxHealth.roundToInt()})",
                    "${CC.YELLOW}Location: ${CC.WHITE}${location.blockX}, ${location.blockY}, ${location.blockZ}",
                    "",
                    "${CC.GREEN}Left-Click to manage roles"
                ).also { button ->
                    if (house.owner != player.uniqueId)
                    {
                        button.addToLore(
                            "${CC.YELLOW}Middle-Click to kick player",
                            "${CC.RED}Right-Click to ban player"
                        )
                    }
                }
                .toButton { _, click ->
                    if (click!!.isLeftClick)
                    {

                    }

                    // don't let people ban or kick themselves
                    if (house.owner != player.uniqueId)
                    {
                        if (click.isCreativeAction)
                        {
                            ConfirmMenu("Kick ${other.name}?") { confirmed ->
                                if (confirmed)
                                {
                                    other.kickPlayer("${CC.RED}You have been kicked from this realm!")
                                    player.sendMessage("${CC.GREEN}Successfully kicked ${other.name}!")
                                }

                                PlayerManagementMenu(house).openMenu(player)
                            }.openMenu(player)
                        } else if (click.isRightClick)
                        {
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
                    }
                }
        }
    }
}