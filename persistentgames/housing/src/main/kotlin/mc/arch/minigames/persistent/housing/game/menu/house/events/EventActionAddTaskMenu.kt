package mc.arch.minigames.persistent.housing.game.menu.house.events

import com.cryptomorin.xseries.XMaterial
import mc.arch.minigames.persistent.housing.api.action.player.ActionEvent
import mc.arch.minigames.persistent.housing.api.model.PlayerHouse
import mc.arch.minigames.persistent.housing.game.actions.HousingActionBukkitImplementation
import mc.arch.minigames.persistent.housing.game.actions.getDisplayBundle
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player

/**
 * Class created on 12/23/2025

 * @author Max C.
 * @project arch-minigames
 * @website https://solo.to/redis
 */
class EventActionAddTaskMenu(val house: PlayerHouse, val event: ActionEvent) : PaginatedMenu()
{
    init
    {
        placeholdBorders = true
    }

    override fun size(buttons: Map<Int, Button>) = 45
    override fun getMaxItemsPerPage(player: Player) = 21
    override fun getAllPagesButtonSlots() = (10..16).toList() + (19..25).toList() + (28..34).toList()

    override fun getPrePaginatedTitle(player: Player): String = "Viewing Event Tasks"

    override fun getGlobalButtons(player: Player): Map<Int, Button> = mapOf(
        40 to ItemBuilder.of(XMaterial.ARROW)
            .name("${CC.GREEN}Go Back")
            .addToLore(
                "${CC.GRAY}Go back to the main action",
                "${CC.GRAY}menu.",
                "",
                "${CC.GREEN}Click to go back"
            )
            .toButton { _, _ ->
                Button.playNeutral(player)
                EventActionTasksMenu(house, event).openMenu(player)
            })

    override fun getAllPagesButtons(player: Player): Map<Int, Button> = mutableMapOf<Int, Button>().also { buttons ->
        HousingActionBukkitImplementation.getAllTasks()
            .filter { it.appliesToEvent(event) }.forEach { task ->
                val displayAttribute = task.getDisplayBundle()
                buttons[buttons.size] = ItemBuilder.of(displayAttribute.icon)
                    .name("${CC.GREEN}${displayAttribute.displayName}")
                    .addToLore("${CC.YELLOW}Click to select this task!")
                    .toButton { _, _ ->
                        if (task.options.isEmpty())
                        {
                            house.addTaskToAction(event, task)
                            player.sendMessage("${CC.B_GREEN}SUCCESS! ${CC.GREEN}You have successfully added this task to your house")
                        } else
                        {

                        }
                    }
            }
    }
}