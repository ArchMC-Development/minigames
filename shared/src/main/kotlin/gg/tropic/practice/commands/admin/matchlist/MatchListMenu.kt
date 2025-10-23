package gg.tropic.practice.commands.admin.matchlist

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.util.QuickAccess.username
import gg.scala.lemon.util.SplitUtil
import gg.tropic.practice.Globals
import gg.tropic.practice.games.GameState
import gg.tropic.practice.kit.KitService
import gg.tropic.practice.metadata.SystemMetadataService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.menus.ConfirmMenu
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * @author GrowlyX
 * @since 1/13/2024
 */
class MatchListMenu : PaginatedMenu()
{
    private var sort = MatchListSort.Kit
    private var filter = MatchListFilter.All

    init
    {
        autoUpdate = true
    }

    override fun getMaxItemsPerPage(player: Player) = 36
    override fun size(buttons: Map<Int, Button>) = 45
    override fun getGlobalButtons(player: Player) = mapOf(
        3 to ItemBuilder
            .of(XMaterial.CLOCK)
            .name("${CC.GREEN}Sort")
            .addToLore(
                "",
                *MatchListSort.entries
                    .map { sort ->
                        if (sort == this.sort)
                        {
                            return@map "${CC.AQUA}${Constants.ARROW_RIGHT} ${sort.name}"
                        }

                        return@map "${CC.GRAY}  ${sort.name}"
                    }
                    .toTypedArray(),
                "",
                "${CC.AQUA}Right-click to go backwards!",
                "${CC.YELLOW}Click to switch sort!"
            )
            .toButton { _, type ->
                sort = if (type!!.isRightClick)
                {
                    sort.previous()
                } else
                {
                    sort.next()
                }

                Button.playNeutral(player)
                openMenu(player)
            },
        5 to ItemBuilder
            .of(XMaterial.HOPPER)
            .name("${CC.GREEN}Filter")
            .addToLore(
                "",
                *MatchListFilter.entries
                    .map { sort ->
                        if (sort == this.filter)
                        {
                            return@map "${CC.AQUA}${Constants.ARROW_RIGHT} ${sort.name}"
                        }

                        return@map "${CC.GRAY}  ${sort.name}"
                    }
                    .toTypedArray(),
                "",
                "${CC.AQUA}Right-click to go backwards!",
                "${CC.YELLOW}Click to switch filter!"
            )
            .toButton { _, type ->
                filter = if (type!!.isRightClick)
                {
                    filter.previous()
                } else
                {
                    filter.next()
                }

                Button.playNeutral(player)
                openMenu(player)
            },
    )

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        var indexCounter = 0

        return sort
            .sort(
                filter.filter(SystemMetadataService.allGames())
            )
            .map { reference ->
                val kit = KitService.cached().kits[reference.kitID]
                    ?.displayIcon
                    ?: XMaterial.PAPER.parseItem()!!

                ItemBuilder
                    .copyOf(kit)
                    .name("${CC.GREEN}#${SplitUtil.splitUuid(reference.uniqueId)}")
                    .addToLore(
                        "${CC.GRAY}Server: ${CC.WHITE}${reference.server}",
                        "${CC.GRAY}State: ${when (reference.state)
                        {
                            GameState.Waiting -> "${CC.GOLD}Waiting"
                            GameState.Starting -> "${CC.YELLOW}Starting"
                            GameState.Playing -> "${CC.GREEN}Playing"
                            GameState.Completed -> "${CC.D_GREEN}Completed"
                        }}",
                        "",
                        "${CC.GRAY}Queue: ${CC.WHITE}${
                            reference.queueId ?: "${CC.RED}Private"
                        }",
                        "",
                        "${CC.GRAY}Map: ${CC.AQUA}${reference.mapID}",
                        "${CC.GRAY}Kit: ${CC.AQUA}${reference.kitID}",
                        "",
                        "${CC.GREEN}Spectators:${
                            if (reference.spectators.isEmpty()) "${CC.RED} None!" else ""
                        }"
                    )
                    .apply {
                        if (reference.spectators.isNotEmpty())
                        {
                            reference.spectators.forEach { spectator ->
                                addToLore("${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.WHITE}${spectator.username()}")
                            }
                        }
                    }
                    .addToLore(
                        "",
                        "${CC.AQUA}Players:${
                            if (reference.players.isEmpty()) "${CC.RED} None!" else ""
                        }"
                    )
                    .apply {
                        if (reference.players.isNotEmpty())
                        {
                            reference.players.forEach { player ->
                                addToLore("${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.WHITE}${
                                    if (player in Globals.POSSIBLE_PLAYER_BOT_UNIQUE_IDS) "${CC.I_WHITE}Robot" else player.username()
                                }")
                            }
                        }
                    }
                    .addToLore(
                        "",
                        "${CC.GREEN}[Click to spectate]",
                        "${CC.RED}[Shift-Click to terminate]"
                    )
                    .toButton { _, type ->
                        if (type!!.isShiftClick)
                        {
                            ConfirmMenu(
                                title = "Confirm match termination",
                                confirm = true,
                                callback = { confirmed ->
                                    if (!confirmed)
                                    {
                                        Tasks.sync { openMenu(player) }
                                        return@ConfirmMenu
                                    }

                                    player.performCommand(
                                        "terminatematch ${reference.players.first().username()}"
                                    )
                                }
                            ).openMenu(player)
                            return@toButton
                        }

                        player.performCommand(
                            "spectate ${reference.players.first().username()}"
                        )
                    }
            }
            .associateBy { indexCounter++ }
    }

    override fun getPrePaginatedTitle(player: Player) = "Viewing all games"
}
