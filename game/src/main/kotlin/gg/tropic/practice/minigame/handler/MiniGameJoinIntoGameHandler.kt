package gg.tropic.practice.minigame.handler

import gg.scala.commons.agnostic.sync.ServerSync
import gg.tropic.practice.games.GameService
import gg.tropic.practice.games.GameState
import gg.tropic.practice.games.matchmaking.JoinIntoGameRequest
import gg.tropic.practice.games.matchmaking.JoinIntoGameResult
import gg.tropic.practice.games.matchmaking.JoinIntoGameStatus
import io.sentry.Sentry
import io.sentry.SpanStatus
import mc.arch.commons.communications.rpc.RPCContext
import mc.arch.commons.communications.rpc.RPCHandler
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.util.CC
import okio.withLock
import org.bukkit.Bukkit

/**
 * @author Subham
 * @since 7/20/25
 */
class MiniGameJoinIntoGameHandler : RPCHandler<JoinIntoGameRequest, JoinIntoGameResult>
{
    override fun handle(
        request: JoinIntoGameRequest,
        context: RPCContext<JoinIntoGameResult>
    )
    {
        // Create child span from RPC transaction (propagated via trace context)
        val span = Sentry.getSpan()?.startChild("handler.join_into_game", "process")
        span?.setData("server", request.server)
        span?.setData("game_id", request.game.uniqueId.toString())
        span?.setData("player_count", request.players.size)
        
        try {
            if (ServerSync.local.id != request.server)
            {
                span?.setData("skipped", "not_target_server")
                span?.status = SpanStatus.OK
                span?.finish()
                return
            }

            val gameImpl = GameService.gameMappings[request.game.uniqueId]
                ?: return run {
                    span?.setData("failure_reason", "game_not_found")
                    span?.status = SpanStatus.NOT_FOUND
                    span?.finish()
                    context.reply(
                        JoinIntoGameResult(
                            status = JoinIntoGameStatus.FAILED_GAME_NOT_FOUND
                        )
                    )
                }

            var status = JoinIntoGameStatus.SUCCESS
            if (!(gameImpl.state(GameState.Waiting) || gameImpl.state(GameState.Starting)))
            {
                status = JoinIntoGameStatus.FAILED_ALREADY_STARTED
            } else
            {
                val teamAssignSpan = span?.startChild("team_assignment", "assign_players")
                gameImpl.teamMutLock.withLock {
                    val maxPlayersPerTeam = gameImpl.miniGameLifecycle!!.configuration.maximumPlayersPerTeam
                    val partySize = request.players.size

                    if (partySize + gameImpl.teams.sumOf { it.players.size } > gameImpl.miniGameLifecycle!!.configuration.maximumPlayers)
                    {
                        status = JoinIntoGameStatus.FAILED_NON_EMPTY_TEAMS
                        return@withLock
                    }

                    request.players.forEach { player ->
                        val team = gameImpl.getNullableTeamOfID(player)
                            ?: return@forEach

                        team.players -= player
                    }

                    // Check if party needs to be split across multiple teams
                    if (partySize > maxPlayersPerTeam)
                    {
                        val availableTeams = gameImpl.teams
                            .filter { team ->
                                team.players.size < maxPlayersPerTeam
                            }
                            .sortedBy { it.players.size }

                        val totalAvailableSlots = availableTeams.sumOf { team ->
                            maxPlayersPerTeam - team.players.size
                        }

                        if (totalAvailableSlots < partySize)
                        {
                            // Not enough total capacity across all teams
                            status = JoinIntoGameStatus.FAILED_NON_EMPTY_TEAMS
                        } else
                        {
                            // Distribute players across available teams
                            val playersToPlace = request.players.toMutableList()

                            for (team in availableTeams)
                            {
                                if (playersToPlace.isEmpty())
                                {
                                    break
                                }

                                val availableSpots = maxPlayersPerTeam - team.players.size
                                val playersToAddToThisTeam = minOf(availableSpots, playersToPlace.size)

                                val playersForThisTeam = playersToPlace.take(playersToAddToThisTeam)
                                playersToPlace.removeAll(playersForThisTeam)

                                gameImpl.preWaitAdd(playersForThisTeam, team.teamIdentifier)
                            }

                            if (playersToPlace.isNotEmpty())
                            {
                                // Couldn't place all players - rollback all placed players
                                request.players.forEach { player ->
                                    val team = gameImpl.getTeamOf(player)
                                    team?.players?.remove(player)
                                }
                                status = JoinIntoGameStatus.FAILED_NON_EMPTY_TEAMS
                            }
                        }
                    } else
                    {
                        // Original logic for parties that fit in one team
                        // Step 1: Check if there are empty teams that can fit the party
                        val emptyTeams = gameImpl.teams.filter { it.players.isEmpty() }
                        if (emptyTeams.isNotEmpty())
                        {
                            val targetTeam = emptyTeams.first()
                            gameImpl.preWaitAdd(request.players.toList(), targetTeam.teamIdentifier)
                        } else
                        {
                            // Step 2: Check if there are non-empty teams that can fit the party
                            val nonEmptyTeamsWithSpace = gameImpl.teams
                                .filter { it.players.isNotEmpty() && it.players.size + partySize <= maxPlayersPerTeam }
                                .sortedBy { it.players.size } // Prefer teams with fewer players for better balance

                            if (nonEmptyTeamsWithSpace.isNotEmpty())
                            {
                                val targetTeam = nonEmptyTeamsWithSpace.first()
                                gameImpl.preWaitAdd(request.players.toList(), targetTeam.teamIdentifier)
                            } else
                            {
                                // Step 3: Try to merge two non-empty teams to make space
                                val nonEmptyTeams = gameImpl.teams
                                    .filter { it.players.isNotEmpty() && it.players.size < maxPlayersPerTeam }
                                    .sortedBy { it.players.size }

                                if (nonEmptyTeams.size >= 2)
                                {
                                    val firstTeam = nonEmptyTeams[0]
                                    val secondTeam = nonEmptyTeams[1]

                                    // Check if we can merge the smaller team into the larger one
                                    // and still have space for the party in the now-empty team
                                    if (secondTeam.players.size + firstTeam.players.size <= maxPlayersPerTeam)
                                    {
                                        // Move all players from first team to second team
                                        firstTeam.players.forEach { player ->
                                            secondTeam.players += player

                                            val bukkitPlayer = Bukkit.getPlayer(player)
                                            if (bukkitPlayer != null)
                                            {
                                                NametagHandler.reloadPlayer(bukkitPlayer)
                                                bukkitPlayer.sendMessage("${CC.RED}You have been switched to a different team to accommodate for a party.")
                                            }
                                        }

                                        // Clear the first team and add the party there
                                        firstTeam.players.clear()

                                        // Verify the party can still fit in the now-empty team
                                        gameImpl.preWaitAdd(request.players.toList(), firstTeam.teamIdentifier)
                                    } else
                                    {
                                        // Can't merge teams - not enough space
                                        status = JoinIntoGameStatus.FAILED_NON_EMPTY_TEAMS
                                    }
                                } else
                                {
                                    // Not enough non-empty teams to perform merge
                                    status = JoinIntoGameStatus.FAILED_NON_EMPTY_TEAMS
                                }
                            }
                        }
                    }
                }
                teamAssignSpan?.status = if (status == JoinIntoGameStatus.SUCCESS) SpanStatus.OK else SpanStatus.INTERNAL_ERROR
                teamAssignSpan?.finish()
            }

            if (status == JoinIntoGameStatus.SUCCESS)
            {
                request.players.forEach { player ->
                    gameImpl.pendingLogins[player] = System.currentTimeMillis()
                }
            }

            span?.setData("result_status", status.name)
            span?.status = if (status == JoinIntoGameStatus.SUCCESS) SpanStatus.OK else SpanStatus.INTERNAL_ERROR
            span?.finish()
            
            context.reply(JoinIntoGameResult(status = status))
        } catch (e: Exception) {
            Sentry.captureException(e)
            span?.throwable = e
            span?.status = SpanStatus.INTERNAL_ERROR
            span?.finish()
            throw e
        }
    }
}
