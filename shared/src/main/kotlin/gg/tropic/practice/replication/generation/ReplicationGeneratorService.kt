package gg.tropic.practice.replication.generation

import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.agnostic.sync.ServerSync
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.tropic.practice.expectation.GameExpectation
import gg.tropic.practice.map.Map
import gg.tropic.practice.replication.ReplicationRPC
import gg.tropic.practice.replication.generation.rpc.GenerationResult
import gg.tropic.practice.replication.ServerAvailableReplicationState
import me.lucko.helper.Schedulers
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 9/24/2023
 */
@Service(priority = 1600)
object ReplicationGeneratorService
{
    @Inject
    lateinit var plugin: ExtendedScalaPlugin

    fun bindToStatusService(statusService: () -> ServerAvailableReplicationState)
    {
        Schedulers
            .async()
            .runRepeating(Runnable {
                ReplicationRPC.updateStatus(
                    server = ServerSync.local.id,
                    status = statusService()
                )
            }, 0L, 10L)
            .bindWith(plugin)

        plugin.logger.info("Bound status service. Status updates for available replications will be pushed to the replicationmanager channel ever 0.5 seconds.")
    }
}
