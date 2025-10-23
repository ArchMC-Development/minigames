package gg.tropic.practice.ugc.creator

import gg.tropic.practice.ugc.HostedWorldInstance
import gg.tropic.practice.ugc.WorldInstanceProviderType
import gg.tropic.practice.ugc.generation.visits.VisitWorldRequest
import gg.tropic.practice.ugc.resources.HostedWorldInstancePlayerResources
import java.util.concurrent.CompletableFuture

/**
 * Provided by the provider of the hosted world instance
 * creator provider that creates a new hosted world
 * instance that providers players with a world to join.
 *
 * @author Subham
 * @since 7/20/25
 */
interface HostedWorldInstanceCreator<R : HostedWorldInstancePlayerResources>
{
    val type: WorldInstanceProviderType

    fun createInstance(request: VisitWorldRequest): CompletableFuture<HostedWorldInstance<R>>
}
