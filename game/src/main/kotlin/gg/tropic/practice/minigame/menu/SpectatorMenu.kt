package gg.tropic.practice.minigame.menu

import com.cryptomorin.xseries.XMaterial
import gg.scala.staff.reports.ReportMenu
import gg.tropic.practice.games.GameImpl
import gg.tropic.practice.games.GameService
import gg.tropic.practice.games.event.SpectatorViewParticipantsEvent
import gg.tropic.practice.minigame.spectators.PlayerParticipant
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 7/1/25
 */
class SpectatorMenu(
    private val player: Player,
    minigame: GameImpl
) : PaginatedMenu()
{
    private val event = SpectatorViewParticipantsEvent(
        game = minigame,
        viewer = player,
        players = minigame.allNonSpectators()
            .map { player ->
                PlayerParticipant(
                    player = player,
                    displayName = "${CC.GREEN}${player.name}",
                    description = listOf(
                        "${CC.GRAY}Health: ${CC.YELLOW}${
                            "%.2f".format(
                                (player.health / player.maxHealth).toFloat() * 100.0f
                            )
                        }",
                        "${CC.GRAY}Hunger: ${CC.YELLOW}${
                            "%.2f".format(
                                (player.foodLevel / 20).toFloat() * 100.0f
                            )
                        }",
                        ""
                    )
                )
            }
    )

    init
    {
        applyHeader = false
        Bukkit.getPluginManager().callEvent(event)
    }

    override fun getMaxItemsPerPage(player: Player) = 21
    override fun size(buttons: Map<Int, Button>) = 45

    override fun getAllPagesButtonSlots() = (10..16) + (19..25) + (28..34)

    override fun getPrePaginatedTitle(player: Player) = "Spectate a player..."

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()
        event.players.forEach { other ->
            buttons[buttons.size] = ItemBuilder.of(XMaterial.PLAYER_HEAD)
                .owner(other.player.name)
                .name("${CC.GREEN}${other.displayName}")
                .setLore(other.description)
                .addToLore(
                    "${CC.GREEN}Click to teleport.",
                    "${CC.RED}Shift-click to report.",
                )
                .data(3)
                .toButton { _, type ->
                    Button.playNeutral(player)
                    if (!GameService.isSpectating(player))
                    {
                        player.sendMessage("${CC.RED}You must a spectator to be able to spectate another player!")
                        return@toButton
                    }

                    if (type!!.isShiftClick)
                    {
                        ReportMenu(other.player.uniqueId).openMenu(player)
                        return@toButton
                    }

                    player.closeInventory()
                    player.teleport(other.player.location)
                }
        }
        return buttons
    }
}
