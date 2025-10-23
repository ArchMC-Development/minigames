package gg.tropic.practice.replications.manager

import com.github.benmanes.caffeine.cache.Caffeine
import gg.tropic.practice.expectation.GameExpectation
import gg.tropic.practice.replication.*
import gg.tropic.practice.replication.generation.rpc.GenerationRequest
import gg.tropic.practice.replication.generation.rpc.GenerationRequirement
import gg.tropic.practice.replication.generation.rpc.GenerationResult
import mc.arch.commons.communications.rpc.addFunctionalHandler
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * @author GrowlyX
 * @since 9/24/2023
 */
object ReplicationManager
{
    private val gameInstanceCache = Caffeine.newBuilder()
        .expireAfterWrite(2L, TimeUnit.SECONDS)
        .build<String, ReplicationServerStatus>()

    fun allServerStatuses() = gameInstanceCache.asMap()

    fun load()
    {
        ReplicationRPC.statusService.addFunctionalHandler { request, context ->
            gameInstanceCache.put(request.server, request)
            context.reply(Unit)
        }
    }

    fun generateReplication(
        server: String,
        map: String,
        requirement: GenerationRequirement,
        expectation: GameExpectation
    ): CompletableFuture<GenerationResult>
    {
        return ReplicationRPC.generationService.call(
            GenerationRequest(
                server = server,
                map = map,
                requirement = requirement,
                expectation = expectation
            )
        ).handle { result, throwable ->
            when
            {
                throwable != null -> GenerationResult(
                    status = ReplicationResultStatus.FAILED,
                    message = "Service did not get response in time: ${throwable.message}"
                )

                result != null -> result
                else -> GenerationResult(
                    status = ReplicationResultStatus.FAILED,
                    message = "System is unavailable (improper backend response)"
                )
            }
        }
    }
}
