package gg.tropic.practice.games.tasks.lifecycle

import com.cryptomorin.xseries.XSound
import gg.tropic.practice.games.*
import gg.tropic.practice.games.event.PlayerRespawnEvent
import gg.tropic.practice.kit.feature.FeatureFlag
import me.lucko.helper.Schedulers
import me.lucko.helper.scheduler.Task
import net.evilblock.cubed.util.CC
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

/**
 * @author GrowlyX
 * @since 7/15/2024
 */
class PlayerRespawnTask(
    private val player: Player,
    private val game: GameImpl,
    private val optionalTime: Int? = null
) : Runnable, AutoCloseable
{
    companion object
    {
        fun GameImpl.startDelayedRespawn(player: Player, optionalTime: Int? = null) =
            with(PlayerRespawnTask(player, this, optionalTime)) {
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

    private var task by Delegates.notNull<Task>()
    private var tick = optionalTime
        ?: game
            .flagMetaData(FeatureFlag.TimeUserSpectatesAfterDeath, "value")
            ?.toIntOrNull()
        ?: 3

    init
    {
        player.allowFlight = true
        player.isFlying = true

        game.enterSpectatorAfterLifeEnd(player)
    }

    override fun run()
    {
        if (!game.state(GameState.Playing) || tick <= 0)
        {
            if (game.prepareForNewLife(player, volatile = false))
            {
                GameService.audiences.player(player).apply {
                    clearTitle()
                    sendActionBar { Component.empty() }
                }

                if (game.flag(FeatureFlag.SpawnProtection))
                {
                    val time = game.flagMetaData(FeatureFlag.SpawnProtection, "time")?.toLongOrNull() ?: 3
                    player.setMetadata(
                        "spawn-protection",
                        FixedMetadataValue(
                            GameService.plugin,
                            System.currentTimeMillis() + (time * 1000L)
                        )
                    )
                }

                Bukkit.getPluginManager().callEvent(PlayerRespawnEvent(game, player))

                player.sendMessage("${CC.GREEN}You have respawned!")
                player.playSound(player.location, XSound.BLOCK_NOTE_BLOCK_PLING.parseSound(), 1.0f, 1.0f)

                task.closeAndReportException()
            } else
            {
                player.sendMessage("${CC.RED}We were unable to respawn you! Use /lobby to return the lobby and join another game.")
            }
            return
        }

        val respawnMessage = "${CC.SEC}Respawning in ${CC.WHITE}$tick${CC.SEC}..."
        GameService.audiences.player(player).apply {
            showTitle(
                Title.title(
                    Component
                        .text("YOU DIED!")
                        .decorate(TextDecoration.BOLD)
                        .color(NamedTextColor.RED),
                    LegacyComponentSerializer
                        .legacySection()
                        .deserialize(respawnMessage),
                    Title.Times.times(
                        Duration.ofMillis(1L),
                        Duration.ofMillis(1500L),
                        Duration.ofMillis(1L)
                    )
                )
            )
        }

        player.sendMessage(respawnMessage)
        player.playSound(player.location, XSound.BLOCK_NOTE_BLOCK_HAT.parseSound(), 1.0f, 1.0f)

        tick -= 1
    }

    override fun close()
    {

    }
}
