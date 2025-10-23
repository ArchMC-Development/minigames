package gg.tropic.practice.menu.manage.top3

import com.cryptomorin.xseries.XMaterial
import gg.scala.commons.spatial.Position
import gg.tropic.practice.configuration.PracticeConfigurationService
import gg.tropic.practice.configuration.minigame.MinigameLeaderboard
import gg.tropic.practice.configuration.minigame.MinigameTopPlayerNPCSet
import gg.tropic.practice.menu.manage.ManageLobbyMenu
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 4/4/25
 */
class ViewTop3LeaderboardsMenu : PaginatedMenu()
{
    init
    {
        placeholdBorders = true
    }

    override fun getPrePaginatedTitle(player: Player) = "Managing Minigame Top3s"
    override fun getAllPagesButtonSlots() = listOf(
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25
    )

    override fun getGlobalButtons(player: Player) =
        mutableMapOf(
            4 to ItemBuilder.Companion
                .of(XMaterial.WRITABLE_BOOK)
                .name("${CC.D_AQUA}Create a Minigame Top3")
                .addToLore(
                    "",
                    "${CC.YELLOW}Click to create!"
                )
                .toButton { _, _ ->
                    PracticeConfigurationService.editAndSave {
                        local().topPlayerNPCSets += MinigameTopPlayerNPCSet()
                    }

                    Button.playNeutral(player)
                    player.closeInventory()
                    player.sendMessage("${CC.GREEN}Created!")
                }
        )

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        PracticeConfigurationService.cached().local().topPlayerNPCSets
            .forEachIndexed { index, leaderboard ->
                buttons[index] = ItemBuilder
                    .of(XMaterial.EGG)
                    .name("${CC.B_YELLOW}#${index + 1}")
                    .addToLore(
                        "${CC.YELLOW}Positions: ",
                        "${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.YELLOW}1st: ${CC.WHITE}${leaderboard.first}",
                        "${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.YELLOW}2nd: ${CC.WHITE}${leaderboard.second}",
                        "${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.YELLOW}3rd: ${CC.WHITE}${leaderboard.third}",
                        "${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.YELLOW}4th - 10th (Hologram): ${leaderboard.hologram}",
                        "",
                        "${CC.YELLOW}Statistic ID: ${CC.WHITE}${leaderboard.statisticID}",
                        "${CC.YELLOW}Display Name: ${CC.WHITE}${leaderboard.displayName}",
                        "${CC.YELLOW}Statistic Display Name: ${CC.WHITE}${leaderboard.statisticDisplayName}",
                        "",
                        "${CC.GREEN}Click to configure!"
                    )
                    .toButton { _, _ ->
                        ManageTop3LeaderboardMenu(index).openMenu(player)
                    }
            }

        return buttons
    }

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose)
        {
            Tasks.sync {
                ManageLobbyMenu().openMenu(player)
            }
        }
    }

    override fun size(buttons: Map<Int, Button>) = 36
    override fun getMaxItemsPerPage(player: Player) = 14
}
