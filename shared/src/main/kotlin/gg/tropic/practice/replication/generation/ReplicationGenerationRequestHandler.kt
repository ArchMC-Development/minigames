package gg.tropic.practice.replication.generation

import gg.scala.commons.agnostic.sync.ServerSync
import gg.tropic.practice.expectation.GameExpectation
import gg.tropic.practice.map.Map
import gg.tropic.practice.map.MapService
import gg.tropic.practice.replication.generation.rpc.GenerationRequest
import gg.tropic.practice.replication.generation.rpc.GenerationResult
import gg.tropic.practice.replication.ReplicationResultStatus
import gg.tropic.practice.replication.generation.rpc.GenerationRequirement
import mc.arch.commons.communications.rpc.RPCContext
import mc.arch.commons.communications.rpc.RPCHandler
import java.util.concurrent.CompletableFuture

/**
 * @author Subham
 * @since 7/20/25
 */
class ReplicationGenerationRequestHandler(
    private val generator: () -> (Map, GameExpectation) -> CompletableFuture<GenerationResult>,
    private val allocator: () -> (Map, GameExpectation) -> CompletableFuture<GenerationResult>
) : RPCHandler<GenerationRequest, GenerationResult>
{
    override fun handle(
        request: GenerationRequest,
        context: RPCContext<GenerationResult>
    )
    {
        if (ServerSync.local.id != request.server)
        {
            return
        }

        val map = MapService.mapWithID(request.map)
            ?: return run {
                context.reply(GenerationResult(
                    status = ReplicationResultStatus.FAILED,
                    message = "Map service did not find the map you were looking for"
                ))
            }

        val replicationGenerationFuture = when (request.requirement)
        {
            GenerationRequirement.GENERATE -> generator()(map, request.expectation)
            GenerationRequirement.ALLOCATE -> allocator()(map, request.expectation)
        }

        replicationGenerationFuture
            .thenAccept {
                if (it == null)
                {
                    throw IllegalStateException("ReplicationManagerService expected a prepared replication, but instead received a null entry for ${request.map}")
                }

                context.reply(it)
            }
            .exceptionally {
                it.printStackTrace()
                context.reply(GenerationResult(
                    status = ReplicationResultStatus.FAILED,
                    message = "System could not prepare a map"
                ))
                return@exceptionally null
            }
            .join()
    }

}
