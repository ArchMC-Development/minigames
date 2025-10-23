package gg.tropic.practice.ugc

import gg.tropic.practice.ugc.generation.WorldLoadRequest
import gg.tropic.practice.ugc.generation.WorldLoadResponse
import gg.tropic.practice.ugc.generation.addplayer.AddPlayerRequest
import gg.tropic.practice.ugc.generation.addplayer.AddPlayerResponse
import gg.tropic.practice.ugc.generation.visits.VisitWorldRequest
import gg.tropic.practice.ugc.generation.visits.VisitWorldResponse
import mc.arch.commons.communications.rpc.CommunicationGateway
import mc.arch.commons.communications.rpc.createRPCService

/**
 * @author Subham
 * @since 7/20/25
 */
object HostedWorldRPC
{
    private val gateway = CommunicationGateway("ugcgateway")

    val worldLoadRPCService = gateway.createRPCService<WorldLoadRequest, WorldLoadResponse>("hostedworlds-load")
    val visitWorldRPCService = gateway.createRPCService<VisitWorldRequest, VisitWorldResponse>("hostedworlds-visit")
    val visitWorldAddPlayerRPCService = gateway.createRPCService<AddPlayerRequest, AddPlayerResponse>("hostedworlds-addplayer")
}
