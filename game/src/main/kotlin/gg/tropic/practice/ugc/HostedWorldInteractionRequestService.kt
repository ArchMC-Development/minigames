package gg.tropic.practice.ugc

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.tropic.practice.ugc.handler.AddPlayerHandler
import gg.tropic.practice.ugc.handler.WorldLoadHandler

/**
 * @author Subham
 * @since 7/18/25
 */
@Service
object HostedWorldInteractionRequestService
{
    @Configure
    fun configure()
    {
        HostedWorldRPC.worldLoadRPCService.addHandler(WorldLoadHandler())
        HostedWorldRPC.visitWorldAddPlayerRPCService.addHandler(AddPlayerHandler())
    }
}
