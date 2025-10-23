package gg.tropic.practice.ugc.instance

import gg.tropic.practice.strategies.WorldEvictionStrategy
import gg.tropic.practice.ugc.HostedWorldInstance
import gg.tropic.practice.versioned.Versioned

/**
 * @author Subham
 * @since 7/20/25
 */
object HostedWorldInstanceLifecycleController
{
    fun load(instance: HostedWorldInstance<*>)
    {
        instance.load()
    }

    fun unload(instance: HostedWorldInstance<*>)
    {
        if (instance.hasUnloaded)
        {
            return
        }

        instance.hasUnloaded = true
        instance.unload()

        Versioned
            .toProvider()
            .getSlimeProvider()
            .saveWorld(instance.loadedWorld.generic)

        WorldEvictionStrategy.evictWorld(
            world = instance.bukkitWorld,
            redirectTarget = instance.providerType.lobbyGroup,
            kickPlayers = true
        )
    }
}
