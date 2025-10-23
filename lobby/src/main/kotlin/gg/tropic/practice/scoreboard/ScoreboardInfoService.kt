package gg.tropic.practice.scoreboard

import gg.scala.commons.agnostic.sync.server.ServerContainer
import gg.scala.commons.agnostic.sync.server.impl.GameServer
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.tropic.practice.configuration.PracticeConfigurationService
import gg.tropic.practice.gameGroup
import gg.tropic.practice.lobbyGroup
import gg.tropic.practice.minigame.MinigameLobby
import gg.tropic.practice.queue.totalPlayersQueued
import gg.tropic.practice.metadata.SystemMetadataService
import gg.tropic.practice.suffixWhenDev
import me.lucko.helper.Schedulers

/**
 * @author GrowlyX
 * @since 9/24/2023
 */
@Service
object ScoreboardInfoService
{
    data class ScoreboardInfo(
        val online: Int = 0,
        val playing: Int = 0,
        val gameServers: Int = 0,
        val queued: Int = 0,
        val meanTPS: Double = 0.0,
        val runningGames: Int = 0,
        val percentagePlaying: Float = 0.0F,
        val euServerTotalPlayers: Int = 0,
        val naServerTotalPlayers: Int = 0
    )

    var scoreboardInfo = ScoreboardInfo()

    @Configure
    fun configure()
    {
        Schedulers
            .async()
            .runRepeating(Runnable {
                runCatching {
                    val lobbyGroup = when (true)
                    {
                        MinigameLobby.isMainLobby() -> "hub"
                        MinigameLobby.isMinigameLobby() -> PracticeConfigurationService.minigameType().provide().lobbyGroup
                        else -> lobbyGroup()
                    }

                    val lobbyServers = ServerContainer
                        .getServersInGroupCasted<GameServer>(lobbyGroup)

                    val gameServers = ServerContainer
                        .getServersInGroupCasted<GameServer>(
                            gameGroup().suffixWhenDev()
                        )

                    val games = SystemMetadataService.allGames()
                        .filter {
                            if (MinigameLobby.isMainLobby())
                            {
                                return@filter true
                            }

                            it.miniGameType == (if (MinigameLobby.isMinigameLobby()) PracticeConfigurationService.minigameType().provide().internalId else null)
                        }

                    val onlineDuelsLobbyPlayers = lobbyServers.sumOf { it.getPlayersCount() ?: 0 }
                    val onlineDuelsPlayers = games.sumOf { it.players.size }
                    val totalPlayers = onlineDuelsLobbyPlayers + onlineDuelsPlayers

                    val percentage = (if (totalPlayers == 0) 0.0F else (onlineDuelsPlayers.toFloat() / totalPlayers.toFloat())) * 100

                    scoreboardInfo = ScoreboardInfo(
                        online = totalPlayers,
                        playing = onlineDuelsPlayers,
                        gameServers = gameServers.size,
                        meanTPS = gameServers.map { it.getTPS()!! }.average(),
                        runningGames = games.count(),
                        percentagePlaying = percentage,
                        queued = totalPlayersQueued().toInt()
                    )
                }
            }, 0L, 20L)
    }
}
