package mc.arch.minigames.persistent.housing.game.creator

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.tropic.practice.ugc.HostedWorldInstance
import gg.tropic.practice.ugc.WorldInstanceProviderType
import gg.tropic.practice.ugc.creator.HostedWorldInstanceCreator
import gg.tropic.practice.ugc.creator.HostedWorldInstanceCreatorRegistry
import gg.tropic.practice.ugc.generation.visits.VisitWorldRequest
import gg.tropic.practice.ugc.strategies.SlimeWorldLoadStrategy
import gg.tropic.practice.versioned.Versioned
import mc.arch.minigames.persistent.housing.game.instance.HousingHostedWorldInstance
import mc.arch.minigames.persistent.housing.game.resources.HousingPlayerResources
import java.util.concurrent.CompletableFuture

@Service
object HousingHostedWorldInstanceCreator : HostedWorldInstanceCreator<HousingPlayerResources>
{
    override val type = WorldInstanceProviderType.REALM

    @Configure
    fun configure()
    {
        HostedWorldInstanceCreatorRegistry.registerCreator(this)
    }

    override fun createInstance(request: VisitWorldRequest): CompletableFuture<HostedWorldInstance<HousingPlayerResources>> = SlimeWorldLoadStrategy
        .loadPersistentWorld(
            providerType = type,
            persistentWorldId = request.worldGlobalId,
            defaultCreator = { id -> Versioned.toProvider().getSlimeProvider().createEmptyHostedWorld(id) }
        )
        .thenApply { world ->
            if (world == null)
            {
                throw IllegalStateException("This realm was not loaded properly")
            }

            HousingHostedWorldInstance(request, world)
        }
}
