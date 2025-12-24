package mc.arch.minigames.persistent.housing.game.menu.house.events.option

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.util.CallbackInputPrompt
import mc.arch.minigames.persistent.housing.api.action.player.ActionEvent
import mc.arch.minigames.persistent.housing.api.action.tasks.Task
import mc.arch.minigames.persistent.housing.api.model.PlayerHouse
import mc.arch.minigames.persistent.housing.game.menu.house.events.EventActionAddTaskMenu
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player
import kotlin.collections.set

/**
 * Class created on 12/23/2025

 * @author Max C.
 * @project arch-minigames
 * @website https://solo.to/redis
 */
class EventActionOptionsEditMenu(val house: PlayerHouse, val event: ActionEvent, val task: Task) : PaginatedMenu()
{
    init
    {
        placeholdBorders = true
    }

    override fun size(buttons: Map<Int, Button>) = 27
    override fun getMaxItemsPerPage(player: Player) = 9
    override fun getAllPagesButtonSlots() = (10..16).toList()

    override fun getPrePaginatedTitle(player: Player): String = "Viewing Options"

    override fun getGlobalButtons(player: Player): Map<Int, Button> = mapOf(
        40 to ItemBuilder.of(XMaterial.ARROW)
            .name("${CC.GREEN}Go Back")
            .addToLore(
                "${CC.GRAY}Go back to the main task",
                "${CC.GRAY}menu.",
                "",
                "${CC.GREEN}Click to go back"
            )
            .toButton { _, _ ->
                Button.playNeutral(player)
                EventActionAddTaskMenu(house, event).openMenu(player)
            })

    override fun getAllPagesButtons(player: Player): Map<Int, Button> = mutableMapOf<Int, Button>().also { buttons ->
        task.options.forEach {
            buttons[buttons.size] = ItemBuilder.of(XMaterial.PAPER)
                .name("${CC.GREEN}${it.value.name}")
                .addToLore(
                    "${CC.YELLOW}Current Value:",
                    "${CC.WHITE}${it.value.data}",
                    "",
                    "${CC.GREEN}Click to change!"
                ).toButton { _, _ ->
                    CallbackInputPrompt("${CC.GREEN}Enter a new value for this option:") { input ->
                        val mutator = it.value.primitive.mutator.invoke(input)

                        if (mutator == null)
                        {
                            player.sendMessage("${CC.RED}${it.value.primitive.errorMessage}")
                            return@CallbackInputPrompt
                        }

                        it.value.data = mutator
                        task.options[it.key] = it.value

                        house.save()
                        player.sendMessage("${CC.B_GREEN}SUCCESS! You have updated this option")

                        EventActionOptionsEditMenu(house, event, task).openMenu(player)
                    }.start(player)
                }
        }
    }
}