package mc.arch.minigames.persistent.housing.game.menu.player

import mc.arch.minigames.persistent.housing.api.model.PlayerHouse
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import org.bukkit.entity.Player

/**
 * Class created on 12/29/2025

 * @author Max C.
 * @project arch-minigames
 * @website https://solo.to/redis
 */
class PlayerInteractViewMenu(val house: PlayerHouse, val player: Player): Menu("Viewing Info: ${player.name}")
{
    init
    {
        placeholder = true
    }

    override fun size(buttons: Map<Int, Button>): Int = 27

    override fun getButtons(player: Player): Map<Int, Button> = mutableMapOf<Int, Button>().also {

    }
}