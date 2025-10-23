package gg.tropic.practice.ugc.handler

import gg.scala.commons.agnostic.sync.ServerSync
import gg.tropic.practice.ugc.HostedWorldInstanceService
import gg.tropic.practice.ugc.HostedWorldState
import gg.tropic.practice.ugc.generation.addplayer.AddPlayerRequest
import gg.tropic.practice.ugc.generation.addplayer.AddPlayerResponse
import gg.tropic.practice.ugc.generation.addplayer.AddPlayerStatus
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
        if (request.server != ServerSync.local.id)
        {
            return
        }

        val hostedWorld = HostedWorldInstanceService
            .instanceByGID(request.globalWorldId)
            ?: return run {
                context.reply(AddPlayerResponse(
                    status = AddPlayerStatus.FAILURE_WORLD_NOT_LOADED
                ))
            }

        if (hostedWorld.state == HostedWorldState.DRAINING)
        {
            context.reply(AddPlayerResponse(
                status = AddPlayerStatus.FAILURE_WORLD_DRAINING
            ))
            return
        }

        request.visitingPlayers.forEach { player ->
            HostedWorldInstanceService.trackPendingLogin(player, hostedWorld)
        }

        context.reply(AddPlayerResponse(
            status = AddPlayerStatus.SUCCESS
        ))
    }
}
