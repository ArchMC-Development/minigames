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

    /**
     * Attempts to use a rejoin token for the player.
     * @return true if successfully added to team, false if team no longer exists
     */
    fun useRejoinToken(uniqueId: UUID): Boolean
    {
        var success = false
        lifecycle.game.teamMutLock.withLock {
            playersExpectedToRejoin.get(uniqueId)?.apply {
                val team = lifecycle.game.teams
                    .firstOrNull { team -> team.teamIdentifier == previousTeam }

                if (team != null) {
                    // Only add to expectationModel if team exists
                    lifecycle.game.expectationModel.players += uniqueId
                    team.players += uniqueId
                    success = true
                } else {
                    // Team no longer exists - report to Sentry async
                    java.util.concurrent.CompletableFuture.runAsync {
                        io.sentry.Sentry.captureMessage("Rejoin token team not found") { scope ->
                            scope.level = io.sentry.SentryLevel.ERROR
                            scope.setTag("alert_type", "rejoin_team_not_found")
                            scope.setTag("map_id", lifecycle.game.mapId)
                            scope.setTag("game_state", lifecycle.game.state.name)
                            scope.setExtra("player_uuid", uniqueId.toString())
                            scope.setExtra("previous_team", previousTeam.label)
                            scope.setExtra("available_teams", lifecycle.game.teams.map { it.teamIdentifier.label }.toString())
                            scope.setExtra("game_id", lifecycle.game.expectation.toString())
                        }
                    }
                }
            }
        }

        ScalaCommons.bundle().globals().redis().sync()
            .del("${namespace()}:minigames:rejoin:${uniqueId}")
        
        return success
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
