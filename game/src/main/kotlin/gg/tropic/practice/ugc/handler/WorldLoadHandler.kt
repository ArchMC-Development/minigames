package gg.tropic.practice.ugc.handler

import gg.scala.commons.agnostic.sync.ServerSync
import gg.tropic.practice.ugc.HostedWorldInstanceService
import gg.tropic.practice.ugc.creator.HostedWorldInstanceCreatorRegistry
import gg.tropic.practice.ugc.generation.WorldLoadRequest
import gg.tropic.practice.ugc.generation.WorldLoadResponse
import gg.tropic.practice.ugc.generation.WorldLoadStatus
import gg.tropic.practice.ugc.instance.HostedWorldInstanceLifecycleController
import mc.arch.commons.communications.rpc.RPCContext
import mc.arch.commons.communications.rpc.RPCHandler
import net.evilblock.cubed.ScalaCommonsSpigot

/**
 * @author Subham
 * @since 7/20/25
 */
class WorldLoadHandler : RPCHandler<WorldLoadRequest, WorldLoadResponse>
{
    override fun handle(
        request: WorldLoadRequest,
        context: RPCContext<WorldLoadResponse>
    )
    {
        if (ServerSync.local.id != request.server)
        {
            return
        }

        if (ScalaCommonsSpigot.instance.isAgonesInstanceDraining)
        {
            context.reply(WorldLoadResponse(
                status = WorldLoadStatus.FAILED_SERVER_DRAINING,
                server = ServerSync.local.id
            ))
            return
        }

        HostedWorldInstanceCreatorRegistry
            .ofType(request.visitWorldRequest.providerType)
            .createInstance(request.visitWorldRequest)
            .whenComplete { instance, throwable ->
                if (instance != null)
                {
                    HostedWorldInstanceLifecycleController.load(instance)
                }

                if (throwable != null || instance == null)
                {
                    throwable?.printStackTrace()
                    context.reply(WorldLoadResponse(
                        status = WorldLoadStatus.FAILED_LOAD_WORLD,
                        server = ServerSync.local.id
                    ))
                    return@whenComplete
                }

                request.visitWorldRequest.visitingPlayers.forEach { player ->
                    HostedWorldInstanceService.trackPendingLogin(player, instance)
                }

                context.reply(WorldLoadResponse(
                    status = WorldLoadStatus.LOADED,
                    server = ServerSync.local.id
                ))
            }
    }

}
