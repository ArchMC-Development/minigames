package gg.tropic.practice.ugc.instance

import gg.scala.lemon.util.SplitUtil
import gg.tropic.practice.games.GameService
import gg.tropic.practice.strategies.MarkSpectatorStrategy
import gg.tropic.practice.ugc.*
import gg.tropic.practice.ugc.resources.HostedWorldInstancePlayerResources
import gg.tropic.practice.versioned.Versioned
import mc.arch.minigames.versioned.generics.worlds.LoadedSlimeWorld
import me.lucko.helper.terminable.composite.CompositeTerminable
import org.bukkit.World
import org.bukkit.entity.Player
import java.lang.AutoCloseable
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Subham
 * @since 7/20/25
 */
abstract class BaseHostedWorldInstance<R : HostedWorldInstancePlayerResources>(
    override val ownerPlayerId: UUID,
    override val loadedWorld: LoadedSlimeWorld,
    override val providerType: WorldInstanceProviderType,
    override val persistence: HostedWorldPersistence?,
    override val globalId: UUID,
    override val persistencePolicy: PersistencePolicy = if (persistence != null)
        PersistencePolicy.PERSISTENT else PersistencePolicy.TEMPORARY,
    override val nameId: String = "hostedworld_instance_${providerType.name.lowercase()}_${
        persistencePolicy.name.lowercase()
    }_${
        SplitUtil.splitUuid(globalId)
    }",
    override var state: HostedWorldState = HostedWorldState.ACTIVE,
    override val loadTime: Long = System.currentTimeMillis(),
    override var initialTimeWhenEmpty: Long = 0L,
    override var lastDrainBroadcast: Long = 0L,
    override var drainStartTime: Long = 0L,
    override var hasUnloaded: Boolean = false
) : HostedWorldInstance<R>, CompositeTerminable
{
    override val playerResources: MutableMap<UUID, R> = ConcurrentHashMap()
    private val backingTerminable = CompositeTerminable.create()

    abstract fun onLoad()
    abstract fun onUnload()

    override fun load()
    {
        HostedWorldInstanceService.registerWorldInstance(this)
        onLoad()
    }

    override fun unload()
    {
        onUnload()
        closeAndReportException()
        HostedWorldInstanceService.unregisterWorldInstance(this)
    }

    fun nonSpectatorPlayers() = bukkitWorld.players
        .filter {
            !GameService.isSpectating(it)
        }

    fun markSpectator(player: Player) = MarkSpectatorStrategy.markSpectator(
        player = player,
        world = bukkitWorld
    )

    fun saveWorld() =
        Versioned
            .toProvider()
            .getSlimeProvider()
            .saveWorld(loadedWorld.generic)


    override fun onlinePlayers() = bukkitWorld.players.toSet()
    override fun close()
    {
        backingTerminable.close()
    }

    override fun with(autoCloseable: AutoCloseable?): CompositeTerminable? =
        backingTerminable.with(autoCloseable)

    override fun cleanup()
    {
        backingTerminable.cleanup()
    }
}
