package gg.tropic.practice.menu

import gg.scala.lemon.util.QuickAccess.username
import gg.tropic.practice.Globals
import gg.tropic.practice.games.GameReport
import gg.tropic.practice.games.GameReportStatus
import gg.tropic.practice.kit.KitService
import gg.tropic.practice.map.MapService
import gg.tropic.practice.reports.menu.SelectPlayerMenu
import net.evilblock.cubed.ScalaCommonsSpigot
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.time.TimeUtil
import org.apache.commons.lang3.time.DurationFormatUtils
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ForkJoinPool

/**
 * @author GrowlyX
 * @since 8/5/2022
 */
class DuelGamesMenu(
    private val reports: List<GameReport>,
    private val of: UUID
) : PaginatedMenu()
{
    init
    {
        placeholdBorders = true
    }

    override fun size(buttons: Map<Int, Button>) = 54
    override fun getAllPagesButtonSlots() = (10..16) + (19..25) + (28..34) + (37..43)
    override fun getMaxItemsPerPage(player: Player) =
        ((10..16) + (19..25) + (28..34) + (37..43)).toList().size

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        for (report in this.reports.sortedByDescending { it.matchDate.time })
        {
            buttons[buttons.size] = ItemBuilder
                .of(Material.PAPER)
                .name(
                    "${CC.PRI}${
                        TimeUtil.formatIntoFullCalendarString(report.matchDate)
                    }"
                )
                .addToLore(
                    "",
                    "${CC.YELLOW}Winner${
                        if (report.winners.size == 1) "" else "s"
                    }: ${CC.GREEN}${
                        if (report.winners.isEmpty()) "N/A" else
                            report.winners.joinToString(", ") {
                                if (it in Globals.POSSIBLE_PLAYER_BOT_UNIQUE_IDS) "Robot" else report.usernameOf(it)
                            }
                    }",
                    "${CC.YELLOW}Loser${
                        if (report.losers.size == 1) "" else "s"
                    }: ${CC.RED}${
                        if (report.losers.isEmpty()) "N/A" else
                            report.losers.joinToString(", ") {
                                if (it in Globals.POSSIBLE_PLAYER_BOT_UNIQUE_IDS) "Robot" else report.usernameOf(it)
                            }
                    }",
                    "",
                    "${CC.YELLOW}Duration: ${CC.WHITE}${
                        DurationFormatUtils
                            .formatDuration(
                                report.duration, "mm:ss"
                            )
                    }",
                    "${CC.YELLOW}Map: ${CC.WHITE}${
                        MapService.mapWithID(report.map)?.displayName ?: "???"
                    }",
                    "${CC.WHITE}Kit: ${CC.WHITE}${
                        KitService.cached().kits[report.kit]?.displayName ?: "???"
                    }",
                    "",
                    "${CC.GREEN}Click to view inventories!"
                )
                .toButton { _, _ ->
                    Button.playNeutral(player)
                    SelectPlayerMenu(report, this).openMenu(player)
                }
        }

        return buttons
    }

    override fun getPrePaginatedTitle(player: Player) = "${
        if (of == player.uniqueId) "Your" else "${of.username()}'s"
    } games"
}
