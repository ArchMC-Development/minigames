package gg.tropic.practice.portal.menu

import com.cryptomorin.xseries.XMaterial
import gg.scala.commons.configurable.editString
import gg.tropic.practice.portal.LobbyPortalService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.menu.buttons.RemoveButton
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.entity.Player
import java.util.UUID

/**
 * @author Subham
 * @since 6/27/25
 */
class ManageLobbyPortalMenu(
    private val index: UUID
) : Menu("Managing Lobby Portal...")
{
    init
    {
        placeholder = true
    }

    override fun getButtons(player: Player) = mapOf(
        10 to editString(
            LobbyPortalService,
            title = "Destination",
            material = XMaterial.CHEST,
            getter = {
                portals[index]!!.destination
            },
            setter = {
                portals[index]!!.destination = it
            }
        ),
        11 to editString(
            LobbyPortalService,
            title = "Target Group",
            material = XMaterial.NAME_TAG,
            getter = {
                portals[index]!!.server
            },
            setter = {
                portals[index]!!.server = it
            }
        ),
        16 to RemoveButton {
            ViewLobbyPortalsMenu().openMenu(player)

            LobbyPortalService.editAndSave {
                portals.remove(index)
            }

            player.sendMessage("${CC.RED}Removed this portal!")
        }
    )

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose)
        {
            Tasks.sync {
                ViewLobbyPortalsMenu().openMenu(player)
            }
        }
    }

    override fun size(buttons: Map<Int, Button>) = 27
}
