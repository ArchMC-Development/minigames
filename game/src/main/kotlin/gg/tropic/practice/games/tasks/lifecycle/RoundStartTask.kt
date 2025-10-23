package gg.tropic.practice.games.tasks.lifecycle

import com.cryptomorin.xseries.XSound
import gg.tropic.practice.games.*
import gg.tropic.practice.games.team.GameTeam
import gg.tropic.practice.kit.feature.FeatureFlag
import gg.tropic.practice.extensions.toRBTeamSide
import me.lucko.helper.Schedulers
import me.lucko.helper.scheduler.Task
import me.lucko.helper.terminable.composite.CompositeTerminable
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.visibility.VisibilityHandler
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.title.Title
import org.bukkit.Sound
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

/**
 * @author GrowlyX
 * @since 7/17/2024
 */
class RoundStartTask(
    private val winner: GameTeam,
    private val game: GameImpl
) : Runnable, AutoCloseable
{
    companion object
    {
        fun GameImpl.startNewRound(winner: GameTeam) = with(RoundStartTask(winner, this)) {
            task = Schedulers.sync()
                .runRepeating(
                    this,
                    0L, TimeUnit.SECONDS,
                    1L, TimeUnit.SECONDS
                )

            game.with(this)
            task.bindWith(game)
        }
    }

    private var rbTeamSide = winner.teamIdentifier.toRBTeamSide()

    private var task by Delegates.notNull<Task>()
    private var tick = game
        .flagMetaData(FeatureFlag.CountDownTimeBeforeRoundStart, "value")
        ?.toIntOrNull() ?: 3

    init
    {
        game.multiRoundGame.switchingRounds = true

        game.multiRoundGame.terminable.closeAndReportException()
        game.multiRoundGame.terminable = CompositeTerminable.create()

        winner.gameLifecycleArbitraryObjectiveProgress += 1

        game.cleanupWorld()
        game.toBukkitPlayers().filterNotNull()
            .forEach {
                GameService.spectatorPlayers -= it.uniqueId
            }

        game.toBukkitPlayers().filterNotNull()
            .forEach {
                VisibilityHandler.update(it)
            }

        game.toBukkitPlayers().filterNotNull()
            .forEach {
                game.prepareForNewLife(it, true)
            }

        game.toBukkitPlayers().filterNotNull()
            .forEach { player ->
                // enter a "light" spectator mode, where they can't interact with anything yet
                game.prepareForNewLife(
                    player,
                    volatile = false,
                    noUpdateVisibility = true
                )

                GameService.spectatorPlayers += player.uniqueId
                GameService.lightSpectatorPlayers += player.uniqueId
            }

        val winnerDisplay = if (winner.players.size == 1)
            winner.toBukkitPlayers().firstOrNull()?.name else rbTeamSide.display

        val opponent = game.getOpponent(winner)
        val primary = "${rbTeamSide.format(winnerDisplay ?: "???")} ${CC.SEC}scored!"
        val subText = " ${CC.I}${rbTeamSide.format(winner.gameLifecycleArbitraryObjectiveProgress.toString())}${CC.R} ${CC.GRAY}- ${CC.I}${
            opponent.teamIdentifier.toRBTeamSide().format(opponent.gameLifecycleArbitraryObjectiveProgress.toString())
        }"

        game.sendMessage("", primary, "${CC.I}$subText", "")
        game.audiences {
            it.showTitle(
                Title.title(
                    LegacyComponentSerializer.legacySection()
                        .deserialize(primary),
                    LegacyComponentSerializer.legacySection()
                        .deserialize(subText),
                    Title.Times.times(
                        Duration.ofMillis(1L),
                        Duration.ofMillis(1500L),
                        Duration.ofMillis(500L)
                    )
                )
            )
        }
    }

    override fun run()
    {
        if (!game.state(GameState.Playing) || tick <= 0)
        {
            game.multiRoundGame.switchingRounds = false
            game.sendMessage("${CC.GREEN}Round started!")
            game.playSound(XSound.ENTITY_FIREWORK_ROCKET_BLAST.parseSound()!!, 1.0f)

            game.toBukkitPlayers().filterNotNull()
                .forEach {
                    GameService.spectatorPlayers -= it.uniqueId
                    GameService.lightSpectatorPlayers -= it.uniqueId

                    VisibilityHandler.update(it)
                    NametagHandler.reloadPlayer(it)
                }

            game.audiences {
                it.clearTitle()
                it.sendActionBar { Component.empty() }
            }

            task.closeAndReportException()
            return
        }

        val respawnMessage = "${CC.SEC}Round starts in ${CC.WHITE}$tick${CC.SEC}..."
        game.sendMessage(respawnMessage)
        game.audiences {
            it.sendActionBar(
                Component.text { text ->
                    text.append(
                        Component
                            .text("Starting in ")
                            .color(NamedTextColor.GREEN)
                    )

                    text.append(
                        Component
                            .text("$tick")
                            .color(NamedTextColor.WHITE)
                            .decorate(TextDecoration.BOLD)
                    )

                    text.append(
                        Component
                            .text("...")
                            .color(NamedTextColor.GREEN)
                    )
                }
            )
        }

        game.playSound(XSound.BLOCK_NOTE_BLOCK_HAT.parseSound()!!, 1.0f)
        tick -= 1
    }

    override fun close()
    {

    }
}
