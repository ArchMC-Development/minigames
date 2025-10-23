package gg.tropic.practice.minigame

import gg.scala.commons.ScalaCommons
import gg.scala.commons.agnostic.sync.ServerSync
import gg.tropic.practice.games.event.GameStartEvent
import gg.tropic.practice.minigame.event.PlayerMiniGameRejoinTokenExpiredEvent
import gg.tropic.practice.minigame.rejoin.RejoinToken
import gg.tropic.practice.minigame.rejoin.TrackedPlayerRejoin
import gg.tropic.practice.namespace
import me.lucko.helper.Events
import me.lucko.helper.Schedulers
import net.evilblock.cubed.serializers.Serializers
import okio.withLock
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Subham
 * @since 6/16/25
 */
class MiniGameDisconnectedPlayerTracker(private val lifecycle: MiniGameLifecycle<*>) : Runnable
{
    private val playersExpectedToRejoin = ConcurrentHashMap<UUID, TrackedPlayerRejoin>()
    fun subscribe()
    {
        if (lifecycle.configuration.shouldBeAbleToReconnect)
        {
            Events
                .subscribe(GameStartEvent::class.java)
                .filter { it.game.identifier == lifecycle.game.identifier }
                .handler {
                    Schedulers.async()
                        .runRepeating(this, 0L, 20L)
                        .bindWith(lifecycle.game)
                }
                .bindWith(lifecycle.game)
        }
    }

    fun saveRejoinToken(player: Player)
    {
        val token = RejoinToken(
            server = ServerSync.local.id,
            expectation = lifecycle.game.expectation,
            expiration = System.currentTimeMillis() + lifecycle.configuration.reconnectThreshold - 1000L,
            gameDescription = lifecycle.configuration.gameDescription
        )

        val team = lifecycle.game.getTeamOf(player)
        playersExpectedToRejoin[player.uniqueId] = TrackedPlayerRejoin(
            team.teamIdentifier,
            System.currentTimeMillis()
        )

        lifecycle.game.teamMutLock.withLock {
            team.players.remove(player.uniqueId)
        }

        ScalaCommons.bundle().globals().redis().sync()
            .psetex(
                "${namespace()}:minigames:rejoin:${player.uniqueId}",
                lifecycle.configuration.reconnectThreshold,
                Serializers.gson.toJson(token)
            )
    }

    fun useRejoinToken(uniqueId: UUID)
    {
        lifecycle.game.teamMutLock.withLock {
            playersExpectedToRejoin.get(uniqueId)?.apply {
                lifecycle.game.expectationModel.players += uniqueId
                lifecycle.game.teams
                    .firstOrNull { team -> team.teamIdentifier == previousTeam }
                    ?.apply {
                        players += uniqueId
                    }
            }
        }

        ScalaCommons.bundle().globals().redis().sync()
            .del("${namespace()}:minigames:rejoin:${uniqueId}")
    }

    override fun run()
    {
        playersExpectedToRejoin.toMap().forEach { (id, model) ->
            if ((model.joinTime + lifecycle.configuration.reconnectThreshold) < System.currentTimeMillis())
            {
                playersExpectedToRejoin.remove(id)

                Schedulers
                    .sync()
                    .run {
                        Bukkit.getPluginManager().callEvent(PlayerMiniGameRejoinTokenExpiredEvent(lifecycle, id, model))
                    }
            }
        }
    }

    fun removeToken(player: Player)
    {
        playersExpectedToRejoin.remove(player.uniqueId)
    }

    fun getRejoinToken(player: UUID) = playersExpectedToRejoin[player]
}
