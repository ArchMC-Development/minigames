package gg.tropic.practice.ugc.creator

import gg.tropic.practice.ugc.WorldInstanceProviderType

/**
 * @author Subham
 * @since 7/20/25
 */
object HostedWorldInstanceCreatorRegistry
{
    private val registry = mutableMapOf<WorldInstanceProviderType, HostedWorldInstanceCreator<*>>()
    fun registerCreator(creator: HostedWorldInstanceCreator<*>)
    {
        registry[creator.type] = creator
    }

    fun ofType(type: WorldInstanceProviderType) = registry[type]!!

    fun availableProviders() = registry.keys
}
