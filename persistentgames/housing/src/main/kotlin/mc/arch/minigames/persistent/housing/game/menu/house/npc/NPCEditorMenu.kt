package mc.arch.minigames.persistent.housing.game.menu.house.npc

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.util.CallbackInputPrompt
import mc.arch.minigames.persistent.housing.api.action.HousingActionService
import mc.arch.minigames.persistent.housing.api.entity.HousingNPC
import mc.arch.minigames.persistent.housing.api.model.PlayerHouse
import mc.arch.minigames.persistent.housing.game.actions.getDisplayBundle
import mc.arch.minigames.persistent.housing.game.entity.HousingEntityService
import mc.arch.minigames.persistent.housing.game.menu.house.MainHouseMenu
import mc.arch.minigames.persistent.housing.game.menu.house.events.EventActionTasksMenu
import mc.arch.minigames.persistent.housing.game.spatial.toWorldPosition
import mc.arch.minigames.persistent.housing.game.translateCC
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player
import kotlin.collections.set

class NPCEditorMenu(val house: PlayerHouse) : PaginatedMenu()
{
    init
    {
        placeholdBorders = true
        updateAfterClick = true
    }

    override fun getGlobalButtons(player: Player): Map<Int, Button> = mutableMapOf(
        4 to ItemBuilder.of(XMaterial.EMERALD)
            .name("${CC.GREEN}Create NPC")
            .addToLore("${CC.YELLOW}Click to create a new NPC!")
            .toButton { _, _ ->
                CallbackInputPrompt("${CC.GREEN}Please type in the name you want this NPC to have:") { input ->
                    val npc = HousingNPC(input, player.location.toWorldPosition())

                    house.houseNPCMap[npc.id] = npc
                    house.save()
                    HousingEntityService.respawnAll(player.world)

                    player.sendMessage("${CC.B_GREEN}SUCCESS! ${CC.GREEN}You have created an npc!")
                    NPCEditorMenu(house).openMenu(player)
                }.start(player)
            },
        31 to MainHouseMenu.mainMenuButton(house)
    )

    override fun size(buttons: Map<Int, Button>) = 36
    override fun getMaxItemsPerPage(player: Player) = 14
    override fun getAllPagesButtonSlots() = (10..16).toList() + (19..25).toList()

    override fun getPrePaginatedTitle(player: Player): String = "Viewing All Events"

    override fun getAllPagesButtons(player: Player): Map<Int, Button> = mutableMapOf<Int, Button>().also { buttons ->
        house.houseNPCMap.values.forEach { npc ->
            buttons[buttons.size] = ItemBuilder.of(XMaterial.VILLAGER_SPAWN_EGG)
                .name("${CC.GREEN}${npc.name}")
                .addToLore(
                    "${CC.YELLOW}Display Name: ${CC.WHITE}${npc.displayName}",
                    "${CC.YELLOW}Command: ${CC.WHITE}${npc.command ?: "${CC.RED}None"}",
                    "",
                    "${CC.YELLOW}Text Above Head:"
                ).also { button ->
                    if (npc.aboveHeadText.isEmpty())
                    {
                        button.addToLore("${CC.GRAY}- ${CC.RED}None")
                    } else
                    {
                        npc.aboveHeadText.forEach {
                            button.addToLore("${CC.GRAY}- ${CC.WHITE}${it.translateCC()}")
                        }
                    }

                    button.addToLore(
                        "",
                        "${CC.GREEN}Left-Click to edit NPC",
                        "${CC.RED}Right-CLick to delete NPC"
                    )
                }.toButton { _, click ->
                    if (click!!.isLeftClick) {
                        NPCSpecificsEditorMenu(house, npc).openMenu(player)
                    } else if (click.isRightClick) {
                        house.houseNPCMap.remove(npc.id)
                        house.save()
                        HousingEntityService.respawnAll(player.world)

                        player.sendMessage("${CC.RED}Deleted NPC!")
                    }
                }
        }
    }
}