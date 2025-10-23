package gg.tropic.practice.minigame.handler

import gg.scala.commons.agnostic.sync.ServerSync
import gg.tropic.practice.games.GameService
import gg.tropic.practice.games.spectate.SpectateRequest
import gg.tropic.practice.games.spectate.SpectateResponse
import gg.tropic.practice.games.spectate.SpectateResponseStatus
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
        if (ServerSync.local.id != request.server)
        {
            return
        }

        val gameImpl = GameService.gameMappings[request.gameId]
            ?: return run {
                context.reply(
                    SpectateResponse(
                        status = SpectateResponseStatus.FAILED_GAME_NOT_FOUND
                    )
                )
            }

        gameImpl.expectedSpectators += request.player
        context.reply(
            SpectateResponse(
                status = SpectateResponseStatus.SUCCESS
            )
        )
    }
}
