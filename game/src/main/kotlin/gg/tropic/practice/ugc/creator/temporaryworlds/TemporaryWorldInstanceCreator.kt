package gg.tropic.practice.ugc.creator.temporaryworlds

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.tropic.practice.ugc.HostedWorldInstance
import gg.tropic.practice.ugc.WorldInstanceProviderType
import gg.tropic.practice.ugc.creator.HostedWorldInstanceCreator
import gg.tropic.practice.ugc.creator.HostedWorldInstanceCreatorRegistry
import gg.tropic.practice.ugc.generation.visits.VisitWorldRequest
import gg.tropic.practice.ugc.instance.temporaryworlds.TemporaryWorldHostedWorldInstance
import gg.tropic.practice.ugc.strategies.SlimeWorldLoadStrategy
import gg.tropic.practice.ugc.temporaryworlds.TemporaryWorldPlayerResourcesWorld
import java.util.concurrent.CompletableFuture

/**
 * @author Subham
 * @since 7/20/25
 */
@Service
object TemporaryWorldInstanceCreator : HostedWorldInstanceCreator<TemporaryWorldPlayerResourcesWorld>
{
    override val type = WorldInstanceProviderType.TEMPORARY_WORLD

    @Configure
    fun configure()
    {
        HostedWorldInstanceCreatorRegistry.registerCreator(this)
    }

    override fun createInstance(request: VisitWorldRequest): CompletableFuture<HostedWorldInstance<TemporaryWorldPlayerResourcesWorld>> = SlimeWorldLoadStrategy
        .createTemporaryWorld(
            providerType = type,
            worldId = request.worldGlobalId
        )
        .thenApply { world ->
            if (world == null)
            {
                throw IllegalStateException("Temporary world was not loaded properly")
            }

            TemporaryWorldHostedWorldInstance(request, world)
        }
}
