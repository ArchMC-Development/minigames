package gg.tropic.practice.replication

import gg.tropic.practice.replication.generation.rpc.GenerationRequest
import gg.tropic.practice.replication.generation.rpc.GenerationResult
import mc.arch.commons.communications.rpc.CommunicationGateway
import mc.arch.commons.communications.rpc.createRPCService

/**
 * @author Subham
 * @since 7/20/25
 */
object ReplicationRPC
{
    private val gateway = CommunicationGateway("replications")
    val generationService = gateway.createRPCService<GenerationRequest, GenerationResult>(
        serviceName = "generate",
        timeoutSeconds = 5L
    )

    val statusService = gateway.createRPCService<ReplicationServerStatus, Unit>(
        serviceName = "status",
        timeoutSeconds = 1L
    )

    fun updateStatus(server: String, status: ServerAvailableReplicationState)
    {
        statusService.call(ReplicationServerStatus(server, status))
    }
}
