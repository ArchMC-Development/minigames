package mc.arch.minigames.persistent.housing.game.menu.house.npc

import mc.arch.minigames.persistent.housing.api.entity.HousingNPC
import mc.arch.minigames.persistent.housing.api.model.PlayerHouse
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import org.bukkit.entity.Player

class NPCSpecificsEditorMenu(val house: PlayerHouse, val npc: HousingNPC) : Menu("Editing NPC...")
{
    init
    {
        placeholder = true
    }

    override fun size(buttons: Map<Int, Button>): Int = 27
    override fun getButtons(player: Player): Map<Int, Button>
    {
        TODO("Not yet implemented")
    }
}