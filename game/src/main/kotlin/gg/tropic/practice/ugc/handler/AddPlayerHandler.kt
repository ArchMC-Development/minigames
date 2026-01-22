package gg.tropic.practice.ugc.handler

import gg.scala.commons.agnostic.sync.ServerSync
import gg.tropic.practice.ugc.HostedWorldInstanceService
import gg.tropic.practice.ugc.HostedWorldState
import gg.tropic.practice.ugc.generation.addplayer.AddPlayerRequest
import gg.tropic.practice.ugc.generation.addplayer.AddPlayerResponse
import gg.tropic.practice.ugc.generation.addplayer.AddPlayerStatus
import io.sentry.Sentry
import io.sentry.SpanStatus
import mc.arch.commons.communications.rpc.RPCContext
import mc.arch.commons.communications.rpc.RPCHandler

/**
 * @author Subham
 * @since 7/24/25
 */
class AddPlayerHandler : RPCHandler<AddPlayerRequest, AddPlayerResponse>
{
    override fun handle(
        request: AddPlayerRequest,
        context: RPCContext<AddPlayerResponse>
    )
    {
        val span = Sentry.getSpan()?.startChild("handler.add_player", "process")
        span?.setData("server", request.server)
        span?.setData("world_global_id", request.globalWorldId.toString())
        span?.setData("visiting_players_count", request.visitingPlayers.size)
        
        try {
            if (request.server != ServerSync.local.id)
            {
                span?.setData("skipped", "not_target_server")
                span?.status = SpanStatus.OK
                span?.finish()
                return
            }

            val hostedWorld = HostedWorldInstanceService
                .instanceByGID(request.globalWorldId)
                ?: return run {
                    span?.setData("failure_reason", "world_not_loaded")
                    span?.status = SpanStatus.NOT_FOUND
                    span?.finish()
                    context.reply(AddPlayerResponse(
                        status = AddPlayerStatus.FAILURE_WORLD_NOT_LOADED
                    ))
                }

            span?.setData("world_state", hostedWorld.state.name)
            
            if (hostedWorld.state == HostedWorldState.DRAINING)
            {
                span?.setData("failure_reason", "world_draining")
                span?.status = SpanStatus.UNAVAILABLE
                span?.finish()
                context.reply(AddPlayerResponse(
                    status = AddPlayerStatus.FAILURE_WORLD_DRAINING
                ))
                return
            }

            val trackSpan = span?.startChild("track_pending_logins")
            request.visitingPlayers.forEach { player ->
                HostedWorldInstanceService.trackPendingLogin(player, hostedWorld)
            }
            trackSpan?.setData("tracked_count", request.visitingPlayers.size)
            trackSpan?.status = SpanStatus.OK
            trackSpan?.finish()

            span?.setData("result_status", AddPlayerStatus.SUCCESS.name)
            span?.status = SpanStatus.OK
            span?.finish()
            
            context.reply(AddPlayerResponse(
                status = AddPlayerStatus.SUCCESS
            ))
        } catch (e: Exception) {
            Sentry.captureException(e)
            span?.throwable = e
            span?.status = SpanStatus.INTERNAL_ERROR
            span?.finish()
            throw e
        }
    }
}
