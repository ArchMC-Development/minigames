package mc.arch.minigames.persistent.housing.game.menu.house.tags

import mc.arch.minigames.persistent.housing.api.model.PlayerHouse
import mc.arch.minigames.persistent.housing.game.menu.house.MainHouseMenu
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.menus.TextEditorMenu
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

class HouseTagsEditorMenu(val house: PlayerHouse): TextEditorMenu(house.tags)
{
    override fun onSave(player: Player, list: List<String>)
    {
        if (list.size > 10)
        {
            player.sendMessage("${CC.RED}You are only allowed to have 10 tags for your realm.")
            Button.playFail(player)
            return
        }

        house.tags = list.toMutableList()
    }

    override fun onClose(player: Player)
    {
        MainHouseMenu(house, true).openMenu(player)
    }

    override fun getPrePaginatedTitle(player: Player): String
    {
        return "Editing Tags"
    }
}