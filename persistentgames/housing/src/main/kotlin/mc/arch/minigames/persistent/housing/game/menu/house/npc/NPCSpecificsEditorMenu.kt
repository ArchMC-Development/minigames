package mc.arch.minigames.persistent.housing.game.menu.house.npc

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.util.CallbackInputPrompt
import mc.arch.minigames.persistent.housing.api.entity.HousingNPC
import mc.arch.minigames.persistent.housing.api.model.PlayerHouse
import mc.arch.minigames.persistent.housing.game.menu.house.roles.RoleEditorMenu
import mc.arch.minigames.persistent.housing.game.spatial.toWorldPosition
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player

class NPCSpecificsEditorMenu(val house: PlayerHouse, val npc: HousingNPC) : Menu("Editing NPC...")
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
                "${CC.YELLOW}Current: ${CC.WHITE}${npc.displayName}",
                "",
                "${CC.GREEN}Click to edit display name"
            )
            .toButton { _, _ ->
                CallbackInputPrompt("${CC.GREEN}Type new display name:") { input ->
                    npc.displayName = input
                    house.save()

                    player.sendMessage("${CC.GREEN}Updated the display name of this NPC!")
                }.start(player)
            },
        11 to ItemBuilder.of(XMaterial.COMMAND_BLOCK)
            .name("${CC.GREEN}Edit Command")
            .addToLore(
                "${CC.YELLOW}Current: ${CC.WHITE}${npc.command ?: "None"}",
                "",
                "${CC.GREEN}Click to edit command"
            )
            .toButton { _, _ ->
                CallbackInputPrompt("${CC.GREEN}Type in the new command (Do not use /):") { input ->
                    npc.command = input
                    house.save()

                    player.sendMessage("${CC.GREEN}Updated the command for this NPC!")
                }.start(player)
            },
        12 to ItemBuilder.of(XMaterial.OAK_SIGN)
            .name("${CC.GREEN}Edit Text Above Head")
            .addToLore(
                "${CC.YELLOW}Current Lines: ${CC.WHITE}${npc.aboveHeadText.size}",
                "",
                "${CC.GREEN}Click to edit lines"
            )
            .toButton { _, _ ->
                EditNPCLinesMenu(npc, house).openMenu(player)
            },
        13 to ItemBuilder.of(XMaterial.PAPER)
            .name("${CC.GREEN}Edit Messages")
            .addToLore(
                "${CC.YELLOW}Current Messages: ${CC.WHITE}${npc.messagesToSend.size}",
                "",
                "${CC.GREEN}Click to edit messages"
            )
            .toButton { _, _ ->
                EditNPCMessagesMenu(npc, house).openMenu(player)
            },
        14 to ItemBuilder.of(XMaterial.PLAYER_HEAD)
            .name("${CC.GREEN}Edit Skin")
            .addToLore(
                "${CC.YELLOW}Current: ${CC.WHITE}${if (npc.skinTexture != null) "Custom" else "Default"}",
                "",
                "${CC.GREEN}Click to edit skin"
            )
            .toButton { _, _ ->
                player.sendMessage("${CC.RED}Feature coming soon!")
            },
        15 to ItemBuilder.of(XMaterial.ITEM_FRAME)
            .name("${CC.GREEN}Toggle Glowing")
            .addToLore(
                "${CC.YELLOW}Current: ${CC.WHITE}${if (npc.glowing) "${CC.GREEN}Yes" else "${CC.RED}No"}",
                "",
                "${CC.GREEN}Click to toggle glowing"
            )
            .toButton { _, _ ->
                npc.glowing = !npc.glowing
                house.save()

                player.sendMessage("${CC.GREEN}Toggled glowing for this NPC!")
            },
        16 to ItemBuilder.of(XMaterial.COMPASS)
            .name("${CC.GREEN}Teleport Here")
            .addToLore(
                "${CC.YELLOW}Updates NPC location to yours",
                "",
                "${CC.GREEN}Click to move NPC"
            )
            .toButton { _, _ ->
                npc.location = player.location.toWorldPosition()
                house.save()

                player.sendMessage("${CC.GREEN}Moved this NPC to your location!")
                openMenu(player)
            },
        23 to ItemBuilder.of(XMaterial.ARROW)
            .name("${CC.GREEN}Go Back")
            .addToLore("${CC.YELLOW}Click to go back")
            .toButton { _, _ ->
                NPCEditorMenu(house).openMenu(player)
            },
        21 to ItemBuilder.of(XMaterial.RED_WOOL)
            .name("${CC.RED}Delete NPC")
            .addToLore("${CC.YELLOW}Click to permanently remove!")
            .toButton { _, _ ->
                house.houseNPCMap.remove(npc.name)
                house.save()

                player.sendMessage("${CC.RED}You have deleted this NPC!")
                NPCEditorMenu(house).openMenu(player)
            }
    )
}