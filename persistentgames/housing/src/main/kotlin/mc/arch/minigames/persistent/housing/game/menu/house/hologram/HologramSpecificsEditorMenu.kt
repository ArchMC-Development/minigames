package mc.arch.minigames.persistent.housing.game.menu.house.hologram

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.util.CallbackInputPrompt
import mc.arch.minigames.persistent.housing.api.entity.HousingHologram
import mc.arch.minigames.persistent.housing.api.model.PlayerHouse
import mc.arch.minigames.persistent.housing.game.entity.HousingEntityService
import mc.arch.minigames.persistent.housing.game.menu.house.roles.RoleEditorMenu
import mc.arch.minigames.persistent.housing.game.spatial.toWorldPosition
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.menu.menus.TextEditorMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player

class HologramSpecificsEditorMenu(val house: PlayerHouse, val hologram: HousingHologram) : Menu("Editing Hologram...")
{
    init
    {
        placeholder = true
        updateAfterClick = true
    }

    override fun size(buttons: Map<Int, Button>): Int = 27

    override fun getButtons(player: Player): Map<Int, Button> = mutableMapOf(
        10 to ItemBuilder.of(XMaterial.OAK_SIGN)
            .name("${CC.GREEN}Edit Lines")
            .addToLore(
                "${CC.YELLOW}Current Lines: ${CC.WHITE}${hologram.lines.size}",
                "",
                "${CC.GREEN}Click to edit lines"
            )
            .toButton { _, _ ->
                EditHologramLinesMenu(hologram, house).openMenu(player)
            },
        11 to ItemBuilder.of(XMaterial.ITEM_FRAME)
            .name("${CC.GREEN}Edit Floating Item")
            .addToLore(
                "${CC.YELLOW}Current: ${CC.WHITE}${if (hologram.floatingItem != null) "${CC.GREEN}Yes" else "${CC.RED}No"}",
                "",
                "${CC.GREEN}Click to edit floating item"
            )
            .toButton { _, _ ->
                player.sendMessage("${CC.RED}Feature coming soon!")
            },
        12 to ItemBuilder.of(XMaterial.COMPASS)
            .name("${CC.GREEN}Teleport Here")
            .addToLore(
                "${CC.YELLOW}Updates Hologram location to yours",
                "",
                "${CC.GREEN}Click to move Hologram"
            )
            .toButton { _, _ ->
                hologram.location = player.location.toWorldPosition()
                house.save()
                HousingEntityService.respawnAll(player.world)

                player.sendMessage("${CC.GREEN}Moved Hologram to your location!")
            },
        15 to ItemBuilder.of(XMaterial.ARROW)
            .name("${CC.GREEN}Go Back")
            .addToLore("${CC.YELLOW}Click to go back")
            .toButton { _, _ ->
                HologramEditorMenu(house).openMenu(player)
            },
        16 to ItemBuilder.of(XMaterial.RED_WOOL)
            .name("${CC.RED}Delete Hologram")
            .addToLore("${CC.YELLOW}Click to permanently remove")
            .toButton { _, _ ->
                house.houseHologramMap.remove(hologram.id)
                house.save()
                HousingEntityService.respawnAll(player.world)

                player.sendMessage("${CC.RED}Deleted Hologram!")
                HologramEditorMenu(house).openMenu(player)
            }
    )
}
