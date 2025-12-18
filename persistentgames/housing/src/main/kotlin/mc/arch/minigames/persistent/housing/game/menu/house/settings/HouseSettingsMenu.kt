package mc.arch.minigames.persistent.housing.game.menu.house.settings

import com.cryptomorin.xseries.XMaterial
import mc.arch.minigames.persistent.housing.api.formatName
import mc.arch.minigames.persistent.housing.api.model.PlayerHouse
import mc.arch.minigames.persistent.housing.api.model.VisitationStatus
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player
import kotlin.collections.set

class HouseSettingsMenu(val house: PlayerHouse): Menu("House Settings")
{
    init
    {
        updateAfterClick = true
    }

    override fun size(buttons: Map<Int, Button>): Int = 36

    override fun getButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        buttons[10] = ItemBuilder.of(XMaterial.PLAYER_HEAD)
            .name("${CC.GREEN}Max Players: ${CC.AQUA}${house.maxPlayers}")
            .addToLore(
                "${CC.GRAY}Change the maximum number of",
                "${CC.GRAY}players allowed in your house",
                "${CC.GRAY}at one time.",
                "",
                "${CC.B_RED}WARNING ${CC.RED}High player counts could",
                "${CC.RED}impact the performance of your",
                "${CC.RED}house",
                "",
                "${CC.GREEN}Left-Click to cycle forward +10",
                "${CC.RED}Right-Click to cycle backward +10"
            ).toButton { _, click ->
                if (click?.isLeftClick == true)
                {
                    house.maxPlayers += 10

                    if (house.maxPlayers >= 200)
                    {
                        house.maxPlayers = 200
                    }
                } else
                {
                    house.maxPlayers -= 10

                    if (house.maxPlayers <= 10)
                    {
                        house.maxPlayers = 10
                    }
                }

                house.save()
            }

        return buttons
    }
}