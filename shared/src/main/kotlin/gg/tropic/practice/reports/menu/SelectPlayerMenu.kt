package gg.tropic.practice.reports.menu

import com.cryptomorin.xseries.XMaterial
import gg.tropic.practice.games.GameReport
import gg.scala.lemon.util.QuickAccess.username
import gg.tropic.practice.Globals
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 9/16/2022
 */
class SelectPlayerMenu(
    private val game: GameReport,
    private val gamesMenu: Menu? = null
) : PaginatedMenu()
{
    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose)
        {
            if (gamesMenu == null)
            {
                return
            }

            Tasks.sync {
                gamesMenu.openMenu(player)
            }
        }
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        game.snapshots
            .entries
            .sortedBy { it.key in game.losers }
            .onEach {
                buttons[buttons.size] = ItemBuilder
                    .of(XMaterial.PLAYER_HEAD)
                    .owner(game.usernameOf(it.key))
                    .name("${CC.B_GREEN}${
                        if (it.key in Globals.POSSIBLE_PLAYER_BOT_UNIQUE_IDS) "Robot" else game.usernameOf(it.key)
                    }")
                    .addToLore(
                        "${CC.YELLOW}Team: ${
                            if (it.key in game.losers) "${CC.RED}Losers" else "${CC.GREEN}Winners"
                        }",
                    )
                    .toButton { _, _ ->
                        Button.playNeutral(player)
                        PlayerViewMenu(game, it.value, gamesMenu).openMenu(player)
                    }
            }

        return buttons
    }

    override fun getPrePaginatedTitle(player: Player) = "Select a player..."
}
