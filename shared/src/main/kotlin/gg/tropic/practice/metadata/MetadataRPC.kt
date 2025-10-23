package gg.tropic.practice.metadata

import mc.arch.commons.communications.rpc.CommunicationGateway
import mc.arch.commons.communications.rpc.createRPCService

/**
 * @author Subham
 * @since 7/20/25
 */
object MetadataRPC
{
    private val gateway = CommunicationGateway("games")
    val statusService = gateway.createRPCService<ServerInstanceMetadata, Unit>(
        serviceName = "status",
        timeoutSeconds = 1L
    )

    fun updateStatus(server: String, status: InstanceMetadata)
    {
        statusService.call(ServerInstanceMetadata(server, status))
    }
}
