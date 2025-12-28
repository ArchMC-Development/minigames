package mc.arch.minigames.persistent.housing.game.menu.house.events

import com.cryptomorin.xseries.XMaterial
import mc.arch.minigames.persistent.housing.api.action.player.ActionEvent
import mc.arch.minigames.persistent.housing.api.model.PlayerHouse
import mc.arch.minigames.persistent.housing.game.actions.getDisplayBundle
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player

class EventActionTasksMenu(val house: PlayerHouse, val event: ActionEvent) : PaginatedMenu()
{
    init
    {
        placeholdBorders = true
        updateAfterClick = true
    }

    override fun size(buttons: Map<Int, Button>) = 45
    override fun getMaxItemsPerPage(player: Player) = 21
    override fun getAllPagesButtonSlots() = (10..16).toList() + (19..25).toList() + (28..34).toList()

    override fun getPrePaginatedTitle(player: Player): String = "Viewing Tasks"

    override fun getGlobalButtons(player: Player): Map<Int, Button> = mapOf(
        39 to ItemBuilder.of(XMaterial.ARROW)
            .name("${CC.GREEN}Go Back")
            .addToLore(
                "${CC.GRAY}Go back to the main action",
                "${CC.GRAY}menu.",
                "",
                "${CC.GREEN}Click to go back"
            )
            .toButton { _, _ ->
                Button.playNeutral(player)
                EventActionSelectionMenu(house).openMenu(player)
            },
        41 to ItemBuilder.of(XMaterial.COMPARATOR)
            .name("${CC.GOLD}Add Task")
            .addToLore(
                "${CC.GRAY}Add a task that triggers when",
                "${CC.GRAY}this event happens.",
                "",
                "${CC.GREEN}Click to add task!"
            ).toButton { _, _ ->
                EventActionAddTaskMenu(house, event).openMenu(player)
            }
    )

    override fun getAllPagesButtons(player: Player): Map<Int, Button> = mutableMapOf<Int, Button>().also { buttons ->
        val tasks = house.actionEventMap[event.id()]
            ?: mutableListOf()

        tasks.forEach { task ->
            val displayBundle = task.getDisplayBundle()

            buttons[buttons.size] = ItemBuilder.of(displayBundle.icon)
                .name("${CC.GREEN}${displayBundle.displayName}")
                .addToLore(
                    "",
                    "${CC.YELLOW}Options:"
                ).also { button ->
                    if (task.options.isNotEmpty())
                    {
                        task.options.forEach {
                            button.addToLore(
                                "${CC.GRAY}${Constants.DOT_SYMBOL} ${CC.WHITE}${it.value.name}",
                                "${CC.GRAY}    - ${it.value.data}"
                            )
                        }
                    } else
                    {
                        button.addToLore("${CC.RED}None Apply!")
                    }
                    
                    button.addToLore(
                        "",
                        "${CC.RED}Right-Click to delete!",
                        "${CC.GREEN}Left-Click to edit!"
                    )
                }.toButton { _, click ->
                    if (click?.isRightClick == true)
                    {
                        house.removeTaskFromAction(task, event)
                        player.sendMessage("${CC.RED}Deleted task!")
                    } else
                    {

                    }
                }
        }
    }
}