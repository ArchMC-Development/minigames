package mc.arch.minigames.persistent.housing.game.menu.player

import com.cryptomorin.xseries.XMaterial
import mc.arch.minigames.persistent.housing.api.model.PlayerHouse
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player

/**
 * Class created on 12/29/2025

 * @author Max C.
 * @project arch-minigames
 * @website https://solo.to/redis
 */
class PlayerInteractViewMenu(val house: PlayerHouse, val other: Player): Menu("Viewing Info: ${other.name}")
{
    init
    {
        placeholder = true
    }

    override fun size(buttons: Map<Int, Button>): Int = 27

    override fun getButtons(player: Player): Map<Int, Button> = mutableMapOf<Int, Button>().also {
        val admin = house.playerIsOrAboveAdministrator(player.uniqueId)

        if (admin)
        {
            buttons[11] = ItemBuilder.of(XMaterial.BARRIER)
                .name("${CC.GREEN}Ban User")
                .addToLore(
                    "${CC.GRAY}Ban this user indefinitely from",
                    "${CC.GRAY}your house!",
                    "",
                    "${CC.YELLOW}Click to deliver the banhammer!"
                ).toButton { _, _ ->
                    house.housingBans.add(other.uniqueId)
                    house.save().join()

                    if (other.isOnline)
                    {
                        other.kickPlayer("${CC.RED}You have been banned from this house!")
                    }

                    player.sendMessage("${CC.WHITE}${other.displayName} ${CC.GREEN}has been permanently banned by ${CC.WHITE}${player.displayName}")
                }

            buttons[15] = ItemBuilder.of(XMaterial.BARRIER)
                .name("${CC.GREEN}Kick User")
                .addToLore(
                    "${CC.GRAY}Kick this user temporarily from",
                    "${CC.GRAY}your house!",
                    "",
                    "${CC.RED}Note: This player can join back after",
                    "",
                    "${CC.YELLOW}Click to deliver the banhammer!"
                ).toButton { _, _ ->
                    if (other.isOnline)
                    {
                        other.kickPlayer("${CC.RED}You have been kicked from this house!")
                    }

                    player.sendMessage("${CC.WHITE}${other.displayName} ${CC.GREEN}has been kicked by ${CC.WHITE}${player.displayName}")
                }
        }
    }
}