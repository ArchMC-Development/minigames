package gg.tropic.practice.replication.generation

import gg.scala.commons.agnostic.sync.ServerSync
import gg.tropic.practice.expectation.GameExpectation
import gg.tropic.practice.map.Map
import gg.tropic.practice.map.MapService
import gg.tropic.practice.replication.generation.rpc.GenerationRequest
import gg.tropic.practice.replication.generation.rpc.GenerationResult
import gg.tropic.practice.replication.ReplicationResultStatus
import gg.tropic.practice.replication.generation.rpc.GenerationRequirement
import io.sentry.Sentry
import io.sentry.SpanStatus
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
        val span = Sentry.getSpan()?.startChild("handler.replication_generate", request.requirement.name)
        span?.setData("server", request.server)
        span?.setData("map", request.map)
        span?.setData("requirement", request.requirement.name)
        span?.setData("players_count", request.expectation.players.size)
        
        try {
            if (ServerSync.local.id != request.server)
            {
                span?.setData("skipped", "not_target_server")
                span?.status = SpanStatus.OK
                span?.finish()
                return
            }

            val map = MapService.mapWithID(request.map)
                ?: return run {
                    span?.setData("failure_reason", "map_not_found")
                    span?.status = SpanStatus.NOT_FOUND
                    span?.finish()
                    context.reply(GenerationResult(
                        status = ReplicationResultStatus.FAILED,
                        message = "Map service did not find the map you were looking for"
                    ))
                }

            val generationSpan = span?.startChild(
                if (request.requirement == GenerationRequirement.GENERATE) "replication.generate" else "replication.allocate",
                map.name
            )
            
            val replicationGenerationFuture = when (request.requirement)
            {
                GenerationRequirement.GENERATE -> generator()(map, request.expectation)
                GenerationRequirement.ALLOCATE -> allocator()(map, request.expectation)
            }

            replicationGenerationFuture
                .thenAccept {
                    if (it == null)
                    {
                        val exception = IllegalStateException("ReplicationManagerService expected a prepared replication, but instead received a null entry for ${request.map}")
                        Sentry.captureException(exception)
                        generationSpan?.throwable = exception
                        generationSpan?.status = SpanStatus.INTERNAL_ERROR
                        generationSpan?.finish()
                        span?.status = SpanStatus.INTERNAL_ERROR
                        span?.finish()
                        throw exception
                    }

                    generationSpan?.setData("result_status", it.status.name)
                    generationSpan?.status = if (it.status == ReplicationResultStatus.COMPLETED) SpanStatus.OK else SpanStatus.INTERNAL_ERROR
                    generationSpan?.finish()
                    span?.setData("result_status", it.status.name)
                    span?.status = if (it.status == ReplicationResultStatus.COMPLETED) SpanStatus.OK else SpanStatus.INTERNAL_ERROR
                    span?.finish()
                    
                    context.reply(it)
                }
                .exceptionally {
                    Sentry.captureException(it)
                    generationSpan?.throwable = it
                    generationSpan?.status = SpanStatus.INTERNAL_ERROR
                    generationSpan?.finish()
                    span?.throwable = it
                    span?.status = SpanStatus.INTERNAL_ERROR
                    span?.finish()
                    
                    it.printStackTrace()
                    context.reply(GenerationResult(
                        status = ReplicationResultStatus.FAILED,
                        message = "System could not prepare a map"
                    ))
                    return@exceptionally null
                }
                .join()
        } catch (e: Exception) {
            Sentry.captureException(e)
            span?.throwable = e
            span?.status = SpanStatus.INTERNAL_ERROR
            span?.finish()
            context.reply(GenerationResult(
                status = ReplicationResultStatus.FAILED,
                message = "Handler threw exception: ${e.message}"
            ))
            throw e
        }
    }

}
