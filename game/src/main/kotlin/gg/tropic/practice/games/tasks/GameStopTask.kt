package gg.tropic.practice.games.tasks

import gg.scala.commons.playerstatus.isVirtuallyInvisibleToSomeExtent
import gg.scala.lemon.util.QuickAccess.username
import gg.tropic.practice.Globals
import gg.tropic.practice.games.GameImpl
import gg.tropic.practice.games.GameReport
import gg.tropic.practice.games.bots.storeForUser
import gg.tropic.practice.games.elo.ELOUpdates
import gg.tropic.practice.games.event.GameCompleteEvent
import gg.tropic.practice.games.teleportToSpawnLocation
import gg.tropic.practice.kit.feature.FeatureFlag
import gg.tropic.practice.map.rating.MapRatingService
import gg.tropic.practice.serializable.Message
import gg.tropic.practice.settings.isASilentSpectator
import gg.tropic.practice.statistics.StatisticChange
import me.lucko.helper.Events
import me.lucko.helper.scheduler.Task
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.Reflection
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.math.Numbers
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import net.md_5.bungee.api.chat.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.math.abs

/**
 * @author GrowlyX
 * @since 8/5/2022
 */
class GameStopTask(
    private val game: GameImpl,
    private val report: GameReport,
    private val eloMappings: CompletableFuture<ELOUpdates>?,
    private val positionUpdates: Map<UUID, CompletableFuture<StatisticChange>>,
    private val terminationReason: String,
    private val playerFeedback: Map<UUID, MutableList<String>>
) : Runnable
{
    lateinit var task: Task
    var currentCountdown = 5

    override fun run()
    {
        kotlin.runCatching { this.runCatching() }
            .onFailure { it.printStackTrace() }
    }

    private fun runCatching()
    {
        if (currentCountdown == 5)
        {
            Bukkit.getPluginManager().callEvent(GameCompleteEvent(game))

            if (game.miniGameLifecycle == null)
            {
                this.game.sendMessage(
                    "",
                    " ${CC.GOLD}Match Overview ${CC.I_GRAY}(Click to view inventories)",
                )
            }

            if (game.flag(FeatureFlag.DeathBelowYAxis))
            {
                val metadata = game
                    .flagMetaData(FeatureFlag.DeathBelowYAxis, "level")
                    ?.toIntOrNull()
                    ?: 0

                for (team in game.teams)
                {
                    team.toBukkitPlayers()
                        .filterNotNull()
                        .forEach {
                            if (it.location.y <= metadata)
                            {
                                val specLocation = game.map.findSpawnLocationMatchingSpec()
                                if (specLocation == null)
                                {
                                    game.teleportToSpawnLocation(it)
                                } else
                                {
                                    it.teleport(specLocation.toLocation(game.arenaWorld))
                                }
                            }
                        }
                }
            }

            fun Message.appendPlayers(players: List<UUID>)
            {
                if (players.isEmpty())
                {
                    return
                }

                for ((index, winner) in players.withIndex())
                {
                    val username = if (winner in Globals.POSSIBLE_PLAYER_BOT_UNIQUE_IDS)
                    {
                        game.robot(winner)?.name() ?: "Robot"
                    } else
                    {
                        game.usernameOf(winner)
                    }

                    withMessage(CC.YELLOW + username)
                        .andHoverOf(
                            "${CC.GREEN}Click to view inventory!"
                        )
                        .andCommandOf(
                            ClickEvent.Action.RUN_COMMAND,
                            "/matchinventory ${report.identifier} $winner"
                        )

                    if (index < players.size - 1)
                    {
                        withMessage(", ")
                    }
                }
            }

            val matchIs1v1 = report.losers.size == 1 &&
                report.winners.size == 1

            val winnerComponent = Message()
                .withMessage(
                    " ${CC.GREEN}Winner${
                        if (this.report.winners.size == 1) "" else "s"
                    }: ${CC.WHITE}${
                        if (report.winners.isEmpty()) "${CC.WHITE}None!" else ""
                    }"
                )

            val loserComponent = Message()
                .withMessage(
                    "${if (!matchIs1v1) " " else ""}${CC.RED}Loser${
                        if (this.report.losers.size == 1) "" else "s"
                    }: ${CC.WHITE}${
                        if (report.losers.isEmpty()) "${CC.WHITE}None!" else ""
                    }"
                )

            winnerComponent.appendPlayers(report.winners)
            loserComponent.appendPlayers(report.losers)

            if (game.miniGameLifecycle == null)
            {
                if (matchIs1v1)
                {
                    val consolidatedMessage = Message()
                    consolidatedMessage.components += winnerComponent.components
                    consolidatedMessage.withMessage(" ${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ")
                    consolidatedMessage.components += loserComponent.components
                    consolidatedMessage.consolidate()

                    game.sendMessage(consolidatedMessage)
                } else
                {
                    val consolidatedMessage = Message()
                    consolidatedMessage.components += winnerComponent.components
                    consolidatedMessage.consolidate()
                    game.sendMessage(consolidatedMessage)

                    val secondMessage = Message()
                    secondMessage.components += loserComponent.components
                    secondMessage.consolidate()
                    game.sendMessage(secondMessage)
                }

                this.game.sendMessage("")

                val spectators = game.expectedSpectators
                    .mapNotNull(Bukkit::getPlayer)
                    .filter {
                        !it.isVirtuallyInvisibleToSomeExtent() && !it.isASilentSpectator()
                    }

                if (spectators.isNotEmpty())
                {
                    game.sendMessage(
                        " ${CC.YELLOW}Spectators ${CC.GRAY}(${
                            spectators.size
                        })${CC.YELLOW}: ${CC.WHITE}${
                            spectators.take(3)
                                .joinToString(
                                    separator = ", ",
                                    transform = Player::getName
                                )
                        }${
                            if (spectators.size > 3) " ${CC.GRAY}(and ${
                                spectators.size - 3
                            } more...)" else ""
                        }",
                        ""
                    )
                }

                if (eloMappings != null)
                {
                    val winner = report.winners.first()
                    val loser = report.losers.last()
                    eloMappings.thenAccept {
                        game.sendMessage(
                            " ${CC.PINK}ELO Updates:",
                            " ${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.GREEN}${game.usernameOf(winner)}:${CC.WHITE} ${it.winner.first} ${CC.GRAY}(${CC.GREEN}+${it.winner.second}${CC.GRAY})",
                            " ${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.RED}${game.usernameOf(loser)}:${CC.WHITE} ${it.loser.first} ${CC.GRAY}(${CC.RED}-${it.winner.second}${CC.GRAY})",
                            ""
                        )
                    }

                    (report.winners + report.losers)
                        .map { it to positionUpdates[it] }
                        .filter { it.second != null }
                        .forEach {
                            it.second!!.thenAcceptAsync { updates ->
                                val player = Bukkit.getPlayer(it.first)
                                    ?: return@thenAcceptAsync

                                player.sendMessage(" ${CC.D_AQUA}Leaderboards:")
                                player.sendMessage(
                                    " ${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.WHITE}Moved: ${
                                        when (true)
                                        {
                                            (updates.new.position == updates.old.position) -> CC.GRAY
                                            (updates.new.position < updates.old.position) -> "${CC.GREEN}${CC.BOLD}▲${CC.GREEN}"
                                            else -> "${CC.RED}${CC.BOLD}▼${CC.RED}"
                                        }
                                    }${
                                        abs(updates.new.position - updates.old.position)
                                    } ${CC.GRAY}(#${
                                        Numbers.format(updates.old.position + 1)
                                    } ${Constants.ARROW_RIGHT}${CC.R}${CC.GRAY} #${
                                        Numbers.format(updates.new.position + 1)
                                    })"
                                )

                                if (updates.next == null)
                                {
                                    player.sendMessage(" ${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.GREEN}You are #1 on the leaderboards!")
                                    player.sendMessage("")
                                    return@thenAcceptAsync
                                }

                                player.sendMessage(
                                    " ${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${
                                        "${CC.SEC}You need ${CC.PRI}${
                                            updates.requiredScore()
                                        }${CC.SEC} ELO to reach ${CC.GREEN}#${
                                            Numbers.format(
                                                updates.next!!.position + 1
                                            )
                                        } ${CC.GRAY}(${
                                            updates.next!!.uniqueId.username()
                                        })${CC.SEC}."
                                    }"
                                )
                                player.sendMessage("")
                            }
                        }
                }
            }

            if (report.winners.isNotEmpty())
            {
                this.game.audiencesIndexed { audience, player ->
                    audience.showTitle(
                        Title.title(
                            Component
                                .text(
                                    if (player in this.report.winners)
                                        "VICTORY!" else "DEFEAT!"
                                )
                                .color(
                                    if (player in this.report.winners)
                                        NamedTextColor.GOLD else NamedTextColor.RED
                                )
                                .decorate(TextDecoration.BOLD),
                            Component
                                .text(
                                    if (player in this.report.winners)
                                    {
                                        if (this.report.winners.size > 1)
                                        {
                                            "You were on the last team standing!"
                                        } else
                                        {
                                            "You were the last player standing!"
                                        }
                                    } else
                                    {
                                        "Better luck next time!"
                                    }
                                )
                                .color(NamedTextColor.GRAY)
                        )
                    )
                }
            }

            if (terminationReason.isNotBlank())
            {
                game.sendMessage(
                    " ${CC.B_RED}✗ ${CC.RED}Your match was terminated!",
                    " ${CC.RED}Reason: ${CC.WHITE}$terminationReason",
                    " ${CC.GRAY}(Your statistics will not change as a result of this)",
                    ""
                )
            }
        }

        if (currentCountdown == 3 && game.miniGameLifecycle == null)
        {
            game.toBukkitPlayers()
                .filterNotNull()
                .forEach { player ->
                    MapRatingService.sendMapRatingRequest(player, game._map)
                        .thenRun {
                            val feedback = playerFeedback[player.uniqueId]
                                ?: return@thenRun

                            if (feedback.isNotEmpty())
                            {
                                feedback.forEach(player::sendMessage)
                            }
                        }
                }
        }

        if (currentCountdown == 2 && game.expectationModel.queueId != "tournament" && game.expectationModel.queueId != "party")
        {
            if (game.miniGameLifecycle != null && game.expectationModel.queueId != null)
            {

            } else if (game.expectationModel.queueId != null)
            {
                val reQueueItem = ItemBuilder
                    .of(Material.PAPER)
                    .name("${CC.GREEN}Click to join queue ${CC.GRAY}(Right-Click)")
                    .build()

                game.toBukkitPlayers()
                    .filterNotNull()
                    .forEach {
                        it.itemInHand = reQueueItem
                        it.updateInventory()
                    }

                Events
                    .subscribe(PlayerInteractEvent::class.java)
                    .filter {
                        it.action.name.contains("RIGHT") &&
                            it.hasItem() && it.item.isSimilar(reQueueItem)
                    }
                    .handler {
                        if (game.robot())
                        {
                            game.botGameMetadata?.storeForUser(it.player.uniqueId)
                        }

                        it.player.itemInHand = ItemStack(Material.AIR)
                        it.player.updateInventory()
                        it.player.sendMessage(
                            "${CC.GREEN}You will be queued again when you return to spawn!"
                        )

                        game.expectedQueueRejoin += it.player.uniqueId
                        Button.playNeutral(it.player)
                    }
                    .bindWith(game)
            }
        }

        if (currentCountdown <= 0)
        {
            task.closeAndReportException()
            this.game.closeAndCleanup()
            return
        }

        currentCountdown--
    }
}
