package mc.arch.minigames.ugc.gateway

import gg.tropic.practice.ugc.HostedWorldRPC

/**
 * @author Subham
 * @since 7/19/25
 */
object HostedWorldGatewayManager
{
    private val worldLoadService = WorldLoadService(HostedWorldRPC.worldLoadRPCService)

    fun load()
    {
        HostedWorldRPC.visitWorldRPCService.addHandler(VisitWorldRequestHandler(worldLoadService))
    }
}
