package gg.tropic.practice.menu.manage.lobbynpc

import com.cryptomorin.xseries.XMaterial
import gg.scala.commons.configurable.editBoolean
import gg.scala.commons.configurable.editEnum
import gg.scala.commons.configurable.editItemStack
import gg.scala.commons.configurable.editPosition
import gg.scala.commons.configurable.editString
import gg.tropic.practice.configuration.PracticeConfigurationService
import gg.tropic.practice.configuration.minigame.type.LobbyNPCSkinType
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.menu.buttons.RemoveButton
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 6/27/25
 */
class ManageMinigameLobbyNPCMenu(
    private val index: Int
) : Menu("Managing Lobby NPC #${index + 1}")
{
    init
    {
        placeholder = true
    }

    override fun getButtons(player: Player) = mapOf(
        10 to editPosition(
            PracticeConfigurationService,
            title = "Spawn Location",
            material = XMaterial.EMERALD,
            getter = {
                local().minigameLobbyNPCs[index].position
            },
            setter = {
                local().minigameLobbyNPCs[index].position = it
            }
        ),
        11 to editString(
            PracticeConfigurationService,
            title = "Command",
            material = XMaterial.COMMAND_BLOCK,
            getter = {
                local().minigameLobbyNPCs[index].command
            },
            setter = {
                local().minigameLobbyNPCs[index].command = it
            }
        ),
        12 to editString(
            PracticeConfigurationService,
            title = "Gamemode Name",
            material = XMaterial.NAME_TAG,
            getter = {
                local().minigameLobbyNPCs[index].gamemodeName
            },
            setter = {
                local().minigameLobbyNPCs[index].gamemodeName = it
            }
        ),
        13 to editString(
            PracticeConfigurationService,
            title = "Replacement",
            material = XMaterial.OAK_SIGN,
            getter = {
                local().minigameLobbyNPCs[index].replacement
            },
            setter = {
                local().minigameLobbyNPCs[index].replacement = it
            }
        ),
        14 to editEnum<LobbyNPCSkinType, _>(
            PracticeConfigurationService,
            title = "Skin Type",
            material = XMaterial.PLAYER_HEAD,
            getter = {
                local().minigameLobbyNPCs[index].skinType
            },
            setter = {
                local().minigameLobbyNPCs[index].skinType = it as LobbyNPCSkinType
            }
        ),
        15 to editItemStack(
            PracticeConfigurationService,
            title = "Held Item",
            getter = {
                local().minigameLobbyNPCs[index].heldItem
            },
            setter = {
                local().minigameLobbyNPCs[index].heldItem = it
            }
        ),
        16 to editBoolean(
            PracticeConfigurationService,
            title = "Newly Released",
            material = XMaterial.DIAMOND,
            getter = {
                local().minigameLobbyNPCs[index].newlyReleased
            },
            setter = {
                local().minigameLobbyNPCs[index].newlyReleased = it
            }
        ),
        19 to editString(
            PracticeConfigurationService,
            title = "Broadcast Label",
            material = XMaterial.ANVIL,
            getter = {
                local().minigameLobbyNPCs[index].broadcastLabel
            },
            setter = {
                local().minigameLobbyNPCs[index].broadcastLabel = it
            }
        ),
        20 to editString(
            PracticeConfigurationService,
            title = "Action Label",
            material = XMaterial.BREWING_STAND,
            getter = {
                local().minigameLobbyNPCs[index].actionLabel
            },
            setter = {
                local().minigameLobbyNPCs[index].actionLabel = it
            }
        ),
        21 to RemoveButton {
            player.closeInventory()
            PracticeConfigurationService.editAndSave {
                local().minigameLobbyNPCs.removeAt(index)
            }

            player.sendMessage("${CC.RED}Removed!")
        }
    )

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose)
        {
            Tasks.sync {
                ViewMinigameLobbyNPCsMenu().openMenu(player)
            }
        }
    }

    override fun size(buttons: Map<Int, Button>) = 36
}
