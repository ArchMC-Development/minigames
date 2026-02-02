package mc.arch.minigames.ugc.gateway

import gg.scala.commons.consensus.Locks
import gg.tropic.practice.games.manager.GameManager
import gg.tropic.practice.ugc.HostedWorldRPC
import io.sentry.Sentry
import io.sentry.SpanStatus
import mc.arch.commons.communications.rpc.RPCContext
import mc.arch.commons.communications.rpc.RPCHandler
import gg.tropic.practice.ugc.generation.WorldLoadStatus
import gg.tropic.practice.ugc.generation.addplayer.AddPlayerRequest
import gg.tropic.practice.ugc.generation.addplayer.AddPlayerStatus
import gg.tropic.practice.ugc.generation.visits.VisitWorldRequest
import gg.tropic.practice.ugc.generation.visits.VisitWorldResponse
import gg.tropic.practice.ugc.generation.visits.VisitWorldStatus

/**
 * @author Subham
 * @since 7/20/25
 */
class VisitWorldRequestHandler(
    private val worldLoadService: WorldLoadService
) : RPCHandler<VisitWorldRequest, VisitWorldResponse>
{
    override fun handle(
        request: VisitWorldRequest,
        context: RPCContext<VisitWorldResponse>
    )
    {
        val span = Sentry.getSpan()?.startChild("handler.visit_world", "process")
        span?.setData("world_global_id", request.worldGlobalId.toString())
        span?.setData("provider_type", request.providerType.name)
        span?.setData("owner_player_id", request.ownerPlayerId.toString())
        span?.setData("visiting_players_count", request.visitingPlayers.size)
        
        try {
            val lockSpan = span?.startChild("consensus.global_lock", "hostedworlds-load")
            val response = Locks.withGlobalLock(
                "hostedworlds-load",
                request.worldGlobalId.toString()
            ) {
                lockSpan?.status = SpanStatus.OK
                lockSpan?.finish()
                
                val existingWorldSpan = span?.startChild("find_existing_world")
                val existingWorldMatchingSpec = GameManager.allHostedWorldInstances()
                    .firstOrNull {
                        it.type == request.providerType &&
                            it.ownerPlayerId == request.ownerPlayerId
                    }
                existingWorldSpan?.setData("found", existingWorldMatchingSpec != null)
                existingWorldSpan?.status = SpanStatus.OK
                existingWorldSpan?.finish()

                if (existingWorldMatchingSpec != null)
                {
                    span?.setData("existing_world_found", true)
                    span?.setData("existing_world_server", existingWorldMatchingSpec.server)
                    
                    val addPlayerSpan = span?.startChild("rpc.add_player", existingWorldMatchingSpec.server)
                    val response = runCatching {
                        HostedWorldRPC.visitWorldAddPlayerRPCService
                            .call(
                                AddPlayerRequest(
                                    globalWorldId = existingWorldMatchingSpec.globalId,
                                    server = existingWorldMatchingSpec.server,
                                    visitingPlayers = request.visitingPlayers
                                )
                            )
                            .join()
                            ?.status
                    }.getOrElse {
                        Sentry.captureException(it)
                        addPlayerSpan?.throwable = it
                        addPlayerSpan?.status = SpanStatus.INTERNAL_ERROR
                        addPlayerSpan?.finish()
                        AddPlayerStatus.FAILURE_WORLD_NOT_LOADED
                    }
                    
                    addPlayerSpan?.setData("result_status", response?.name ?: "null")
                    addPlayerSpan?.status = if (response == AddPlayerStatus.SUCCESS) SpanStatus.OK else SpanStatus.INTERNAL_ERROR
                    addPlayerSpan?.finish()

                    if (response == AddPlayerStatus.SUCCESS)
                    {
                        return@withGlobalLock VisitWorldResponse(
                            status = VisitWorldStatus.SUCCESS_REDIRECT,
                            redirectToInstance = existingWorldMatchingSpec.server
                        )
                    } else if (response == AddPlayerStatus.FAILURE_WORLD_DRAINING)
                    {
                        return@withGlobalLock VisitWorldResponse(
                            status = VisitWorldStatus.FAILED_UNAVAILABLE_INSTANCE_DRAINING,
                            redirectToInstance = null
                        )
                    }
                }

                span?.setData("existing_world_found", false)
                
                val loadSpan = span?.startChild("world_load_service.load")
                val loadResponse = worldLoadService
                    .loadWorldInstanceOnServerSync(request)
                    .join()
                    ?: return@withGlobalLock run {
                        loadSpan?.setData("failure_reason", "null_response")
                        loadSpan?.status = SpanStatus.INTERNAL_ERROR
                        loadSpan?.finish()
                        VisitWorldResponse(
                            status = VisitWorldStatus.FAILED_RPC_FAILURE
                        )
                    }
                
                loadSpan?.setData("load_status", loadResponse.status.name)
                loadSpan?.setData("target_server", loadResponse.server ?: "unknown")

                if (loadResponse.status == WorldLoadStatus.LOADED)
                {
                    loadSpan?.status = SpanStatus.OK
                    loadSpan?.finish()
                    VisitWorldResponse(
                        status = VisitWorldStatus.SUCCESS_REDIRECT,
                        redirectToInstance = loadResponse.server
                    )
                } else
                {
                    loadSpan?.status = SpanStatus.INTERNAL_ERROR
                    loadSpan?.finish()
                    VisitWorldResponse(
                        status = VisitWorldStatus.FAILED_WORLD_GENERATION_FAILURE,
                        redirectToInstance = null
                    )
                }
            }.join() ?: VisitWorldResponse(
                status = VisitWorldStatus.FAILED_NO_SERVERS_AVAILABLE,
                redirectToInstance = null
            )

            span?.setData("result_status", response.status.name)
            span?.status = if (response.status == VisitWorldStatus.SUCCESS_REDIRECT) SpanStatus.OK else SpanStatus.INTERNAL_ERROR
            span?.finish()
            
            context.reply(response)
        } catch (e: Exception) {
            Sentry.captureException(e)
            span?.throwable = e
            span?.status = SpanStatus.INTERNAL_ERROR
            span?.finish()
            throw e
        }
    }
}
