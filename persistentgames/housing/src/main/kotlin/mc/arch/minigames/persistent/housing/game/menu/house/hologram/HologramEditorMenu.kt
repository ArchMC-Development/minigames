package mc.arch.minigames.persistent.housing.game.menu.house.hologram

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.util.CallbackInputPrompt
import mc.arch.minigames.persistent.housing.api.entity.HousingHologram
import mc.arch.minigames.persistent.housing.api.model.PlayerHouse
import mc.arch.minigames.persistent.housing.game.entity.HousingEntityService
import mc.arch.minigames.persistent.housing.game.menu.house.MainHouseMenu
import mc.arch.minigames.persistent.housing.game.spatial.toWorldPosition
import mc.arch.minigames.persistent.housing.game.translateCC
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player
import kotlin.collections.set

class HologramEditorMenu(val house: PlayerHouse) : PaginatedMenu()
{
    init
    {
        placeholdBorders = true
    }

    override fun getGlobalButtons(player: Player): Map<Int, Button> = mutableMapOf(
        4 to ItemBuilder.of(XMaterial.EMERALD)
            .name("${CC.GREEN}Create Hologram")
            .addToLore("${CC.YELLOW}Click to create a new Hologram!")
            .toButton { _, _ ->
                CallbackInputPrompt("${CC.GREEN}Please type in the name you want this Hologram to have:") { input ->
                    val hologram = HousingHologram(input, player.location.toWorldPosition())

                    house.houseHologramMap[hologram.id] = hologram
                    house.save()
                    HousingEntityService.recalculateAll(house, player.world)

                    player.sendMessage("${CC.B_GREEN}SUCCESS! ${CC.GREEN}You have created a hologram!")
                    HologramEditorMenu(house).openMenu(player)
                }.start(player)
            },
        31 to MainHouseMenu.mainMenuButton(house)
    )

    override fun size(buttons: Map<Int, Button>) = 36
    override fun getMaxItemsPerPage(player: Player) = 14
    override fun getAllPagesButtonSlots() = (10..16).toList() + (19..25).toList()

    override fun getPrePaginatedTitle(player: Player): String = "Viewing All Holograms"

    override fun getAllPagesButtons(player: Player): Map<Int, Button> = mutableMapOf<Int, Button>().also { buttons ->
        house.houseHologramMap.values.forEach { hologram ->
            buttons[buttons.size] = ItemBuilder.of(XMaterial.NAME_TAG)
                .name("${CC.GREEN}${hologram.name}")
                .addToLore(
                    "${CC.YELLOW}Lines:",
                ).also { button ->
                    if (hologram.lines.isEmpty())
                    {
                        button.addToLore("${CC.GRAY}- ${CC.RED}None")
                    } else
                    {
                        hologram.lines.forEach {
                            button.addToLore("${CC.GRAY}- ${CC.WHITE}${it.translateCC()}")
                        }
                    }

                    button.addToLore(
                        "",
                        "${CC.GREEN}Left-Click to edit Hologram",
                        "${CC.RED}Right-CLick to delete Hologram"
                    )
                }.toButton { _, click ->
                    if (click!!.isLeftClick) {
                        HologramSpecificsEditorMenu(house, hologram).openMenu(player)
                    } else if (click.isRightClick) {
                         house.houseHologramMap.remove(hologram.name)
                         house.save()
                         HousingEntityService.recalculateAll(house, player.world)

                         player.sendMessage("${CC.RED}Deleted Hologram!")
                         HologramEditorMenu(house).openMenu(player)
                    }
                }
        }
    }
}
