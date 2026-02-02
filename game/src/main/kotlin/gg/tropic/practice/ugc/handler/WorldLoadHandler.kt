package gg.tropic.practice.ugc.handler

import gg.scala.commons.agnostic.sync.ServerSync
import gg.tropic.practice.ugc.HostedWorldInstanceService
import gg.tropic.practice.ugc.creator.HostedWorldInstanceCreatorRegistry
import gg.tropic.practice.ugc.generation.WorldLoadRequest
import gg.tropic.practice.ugc.generation.WorldLoadResponse
import gg.tropic.practice.ugc.generation.WorldLoadStatus
import gg.tropic.practice.ugc.instance.HostedWorldInstanceLifecycleController
import io.sentry.Sentry
import io.sentry.SpanStatus
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
        val span = Sentry.getSpan()?.startChild("handler.world_load", "process")
        span?.setData("server", request.server)
        span?.setData("provider_type", request.visitWorldRequest.providerType.name)
        span?.setData("world_global_id", request.visitWorldRequest.worldGlobalId.toString())
        span?.setData("visiting_players_count", request.visitWorldRequest.visitingPlayers.size)
        
        try {
            if (ServerSync.local.id != request.server)
            {
                span?.setData("skipped", "not_target_server")
                span?.status = SpanStatus.OK
                span?.finish()
                return
            }

            if (ScalaCommonsSpigot.instance.isAgonesInstanceDraining)
            {
                span?.setData("failure_reason", "server_draining")
                span?.status = SpanStatus.UNAVAILABLE
                span?.finish()
                context.reply(WorldLoadResponse(
                    status = WorldLoadStatus.FAILED_SERVER_DRAINING,
                    server = ServerSync.local.id
                ))
                return
            }

            val createSpan = span?.startChild("world_instance.create", request.visitWorldRequest.providerType.name)
            HostedWorldInstanceCreatorRegistry
                .ofType(request.visitWorldRequest.providerType)
                .createInstance(request.visitWorldRequest)
                .whenComplete { instance, throwable ->
                    if (instance != null)
                    {
                        val loadSpan = createSpan?.startChild("world_instance.load")
                        HostedWorldInstanceLifecycleController.load(instance)
                        loadSpan?.status = SpanStatus.OK
                        loadSpan?.finish()
                    }

                    if (throwable != null || instance == null)
                    {
                        if (throwable != null) {
                            Sentry.captureException(throwable)
                            createSpan?.throwable = throwable
                        }
                        createSpan?.setData("failure_reason", throwable?.message ?: "null_instance")
                        createSpan?.status = SpanStatus.INTERNAL_ERROR
                        createSpan?.finish()
                        span?.status = SpanStatus.INTERNAL_ERROR
                        span?.finish()
                        
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

                    createSpan?.status = SpanStatus.OK
                    createSpan?.finish()
                    span?.setData("result_status", WorldLoadStatus.LOADED.name)
                    span?.status = SpanStatus.OK
                    span?.finish()
                    
                    context.reply(WorldLoadResponse(
                        status = WorldLoadStatus.LOADED,
                        server = ServerSync.local.id
                    ))
                }
        } catch (e: Exception) {
            Sentry.captureException(e)
            span?.throwable = e
            span?.status = SpanStatus.INTERNAL_ERROR
            span?.finish()
            throw e
        }
    }

}
