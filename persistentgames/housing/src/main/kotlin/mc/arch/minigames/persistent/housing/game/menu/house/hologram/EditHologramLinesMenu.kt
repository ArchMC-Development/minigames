package mc.arch.minigames.persistent.housing.game.menu.house.hologram

import mc.arch.minigames.persistent.housing.api.entity.HousingHologram
import mc.arch.minigames.persistent.housing.api.model.PlayerHouse
import mc.arch.minigames.persistent.housing.game.entity.HousingEntityService
import net.evilblock.cubed.menu.menus.TextEditorMenu
import org.bukkit.entity.Player

/**
 * Class created on 1/4/2026

 * @author Max C.
 * @project arch-minigames
 * @website https://solo.to/redis
 */
class EditHologramLinesMenu(val hologram: HousingHologram, val house: PlayerHouse): TextEditorMenu(hologram.lines)
{
    override fun onSave(player: Player, list: List<String>)
    {
        hologram.lines = list.toMutableList()
        house.save()
        HousingEntityService.respawnAll(player.world)
    }

    override fun onClose(player: Player)
    {
        HologramSpecificsEditorMenu(house, hologram).openMenu(player)
    }

    override fun getPrePaginatedTitle(player: Player): String
    {
        return "Editing Hologram"
    }
}