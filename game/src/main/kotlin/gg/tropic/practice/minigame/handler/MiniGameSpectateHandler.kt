package gg.tropic.practice.minigame.handler

import gg.scala.commons.agnostic.sync.ServerSync
import gg.tropic.practice.games.GameService
import gg.tropic.practice.games.spectate.SpectateRequest
import gg.tropic.practice.games.spectate.SpectateResponse
import gg.tropic.practice.games.spectate.SpectateResponseStatus
import io.sentry.Sentry
import io.sentry.SpanStatus
import mc.arch.commons.communications.rpc.RPCContext
import mc.arch.commons.communications.rpc.RPCHandler

/**
 * @author Subham
 * @since 7/20/25
 */
class MiniGameSpectateHandler : RPCHandler<SpectateRequest, SpectateResponse>
{
    override fun handle(
        request: SpectateRequest,
        context: RPCContext<SpectateResponse>
    )
    {
        // Create child span from RPC transaction (propagated via trace context)
        val span = Sentry.getSpan()?.startChild("handler.spectate", "process")
        span?.setData("server", request.server)
        span?.setData("game_id", request.gameId.toString())
        span?.setData("player", request.player.toString())
        
        try {
            if (ServerSync.local.id != request.server)
            {
                span?.setData("skipped", "not_target_server")
                span?.status = SpanStatus.OK
                span?.finish()
                return
            }

            val gameImpl = GameService.gameMappings[request.gameId]
                ?: return run {
                    span?.setData("failure_reason", "game_not_found")
                    span?.status = SpanStatus.NOT_FOUND
                    span?.finish()
                    context.reply(
                        SpectateResponse(
                            status = SpectateResponseStatus.FAILED_GAME_NOT_FOUND
                        )
                    )
                }

            gameImpl.expectedSpectators += request.player
            
            span?.setData("result_status", SpectateResponseStatus.SUCCESS.name)
            span?.status = SpanStatus.OK
            span?.finish()
            
            context.reply(
                SpectateResponse(
                    status = SpectateResponseStatus.SUCCESS
                )
            )
        } catch (e: Exception) {
            Sentry.captureException(e)
            span?.throwable = e
            span?.status = SpanStatus.INTERNAL_ERROR
            span?.finish()
            throw e
        }
    }
}
