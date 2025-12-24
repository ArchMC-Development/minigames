package mc.arch.minigames.persistent.housing.game.menu.house.events

import mc.arch.minigames.persistent.housing.api.action.HousingActionService
import mc.arch.minigames.persistent.housing.api.model.PlayerHouse
import mc.arch.minigames.persistent.housing.game.actions.getDisplayBundle
import mc.arch.minigames.persistent.housing.game.menu.house.MainHouseMenu
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player
import kotlin.collections.set

class EventActionSelectionMenu(val house: PlayerHouse) : PaginatedMenu()
{
    init
    {
        placeholdBorders = true
    }

    override fun getGlobalButtons(player: Player): Map<Int, Button> = mutableMapOf(
        40 to MainHouseMenu.mainMenuButton(house)
    )

    override fun size(buttons: Map<Int, Button>) = 45
    override fun getMaxItemsPerPage(player: Player) = 21
    override fun getAllPagesButtonSlots() = (10..16).toList() + (19..25).toList() + (28..34).toList()

    override fun getPrePaginatedTitle(player: Player): String = "Viewing All Events"

    override fun getAllPagesButtons(player: Player): Map<Int, Button> = mutableMapOf<Int, Button>().also { buttons ->
        HousingActionService.getAllEvents().forEach {
            val displayBundle = it.getDisplayBundle()

            buttons[buttons.size] = ItemBuilder.of(displayBundle.icon)
                .name("${CC.GREEN}${displayBundle.displayName}")
                .addToLore(
                    "${CC.YELLOW}Click to select this event!"
                ).toButton { _, _ ->
                    Button.playNeutral(player)
                    EventActionTasksMenu(house, it).openMenu(player)
                }
        }
    }
}