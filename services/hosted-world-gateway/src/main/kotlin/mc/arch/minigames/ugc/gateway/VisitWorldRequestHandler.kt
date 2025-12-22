package mc.arch.minigames.ugc.gateway

import gg.scala.commons.consensus.Locks
import gg.tropic.practice.games.manager.GameManager
import gg.tropic.practice.ugc.HostedWorldRPC
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
        val response = Locks.withGlobalLock(
            "hostedworlds-load",
            request.worldGlobalId.toString()
        ) {
            val existingWorldMatchingSpec = GameManager.allHostedWorldInstances()
                .firstOrNull {
                    it.type == request.providerType &&
                        it.ownerPlayerId == request.ownerPlayerId
                }

            if (existingWorldMatchingSpec != null)
            {
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
                    AddPlayerStatus.FAILURE_WORLD_NOT_LOADED
                }

                if (response == AddPlayerStatus.SUCCESS)
                {
                    return@withGlobalLock VisitWorldResponse(
                        status = VisitWorldStatus.SUCCESS_REDIRECT,
                        redirectToInstance = existingWorldMatchingSpec.server // Assuming this field exists
                    )
                } else if (response == AddPlayerStatus.FAILURE_WORLD_DRAINING)
                {
                    return@withGlobalLock VisitWorldResponse(
                        status = VisitWorldStatus.FAILED_UNAVAILABLE_INSTANCE_DRAINING,
                        redirectToInstance = null
                    )
                }
            }

            val loadResponse = worldLoadService
                .loadWorldInstanceOnServerSync(request)
                .join()
                ?: return@withGlobalLock VisitWorldResponse(
                    status = VisitWorldStatus.FAILED_RPC_FAILURE
                )

            if (loadResponse.status == WorldLoadStatus.LOADED)
            {
                VisitWorldResponse(
                    status = VisitWorldStatus.SUCCESS_REDIRECT,
                    redirectToInstance = loadResponse.server // Assuming this field exists
                )
            } else
            {
                VisitWorldResponse(
                    status = VisitWorldStatus.FAILED_WORLD_GENERATION_FAILURE,
                    redirectToInstance = null
                )
            }
        }.join() ?: VisitWorldResponse(
            status = VisitWorldStatus.FAILED_NO_SERVERS_AVAILABLE,
            redirectToInstance = null
        )

        context.reply(response)
    }
}
