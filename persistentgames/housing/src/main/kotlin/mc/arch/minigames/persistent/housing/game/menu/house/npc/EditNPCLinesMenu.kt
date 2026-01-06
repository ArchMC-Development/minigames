package mc.arch.minigames.persistent.housing.game.menu.house.npc

import mc.arch.minigames.persistent.housing.api.entity.HousingHologram
import mc.arch.minigames.persistent.housing.api.entity.HousingNPC
import mc.arch.minigames.persistent.housing.api.model.PlayerHouse
import mc.arch.minigames.persistent.housing.game.entity.HousingEntityService
import mc.arch.minigames.persistent.housing.game.menu.house.hologram.HologramSpecificsEditorMenu
import net.evilblock.cubed.menu.menus.TextEditorMenu
import org.bukkit.entity.Player

/**
 * Class created on 1/4/2026

 * @author Max C.
 * @project arch-minigames
 * @website https://solo.to/redis
 */
class EditNPCLinesMenu(val npc: HousingNPC, val house: PlayerHouse): TextEditorMenu(npc.aboveHeadText)
{
    override fun onSave(player: Player, list: List<String>)
    {
        npc.aboveHeadText = list.toMutableList()
        house.save()
        HousingEntityService.respawnAll(player.world)
    }

    override fun onClose(player: Player)
    {
        NPCSpecificsEditorMenu(house, npc).openMenu(player)
    }

    override fun getPrePaginatedTitle(player: Player): String
    {
        return "Editing NPC"
    }
}