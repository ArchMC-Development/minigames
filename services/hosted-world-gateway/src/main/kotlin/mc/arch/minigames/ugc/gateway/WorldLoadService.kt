package mc.arch.minigames.ugc.gateway

import gg.scala.commons.agnostic.sync.ServerSync
import gg.tropic.practice.games.manager.strategies.ServerSelectionStrategy
import gg.tropic.practice.provider.MiniProviderType
import gg.tropic.practice.provider.MiniProviderVersion
import gg.tropic.practice.region.Region
import gg.tropic.practice.ugc.HostedWorldRPC
import mc.arch.commons.communications.rpc.RPCService
import gg.tropic.practice.ugc.generation.WorldLoadRequest
import gg.tropic.practice.ugc.generation.WorldLoadResponse
import gg.tropic.practice.ugc.generation.WorldLoadStatus
import gg.tropic.practice.ugc.generation.visits.VisitWorldRequest
import java.util.concurrent.CompletableFuture

/**
 * @author Subham
 * @since 7/20/25
 */
class WorldLoadService(
    private val rpcService: RPCService<WorldLoadRequest, WorldLoadResponse>
)
{
    fun loadWorldInstanceOnServerSync(request: VisitWorldRequest): CompletableFuture<WorldLoadResponse>
    {
        val instanceSelection = ServerSelectionStrategy
            .select(
                requiredVersion = MiniProviderVersion.LEGACY,
                requiredType = MiniProviderType.UGC,
                region = Region.NA,
                hostedWorldInstanceProviderType = request.providerType
            )
            ?: return CompletableFuture.completedFuture(
                WorldLoadResponse(
                    status = WorldLoadStatus.FAILED_NO_SERVERS_FOUND,
                    server = null
                )
            )

        return rpcService.call(
            WorldLoadRequest(
                visitWorldRequest = request,
                server = instanceSelection.id
            )
        )
    }
}
