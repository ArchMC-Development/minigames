package gg.tropic.practice.ugc

import com.google.common.cache.CacheBuilder
import gg.scala.commons.infrastructure.RollingUpdateStartEvent
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.tropic.practice.ugc.generation.WorldLoadStatus
import gg.tropic.practice.ugc.instance.HostedWorldInstanceLifecycleController
import gg.tropic.practice.versioned.Versioned
import me.lucko.helper.Events
import me.lucko.helper.Schedulers
import net.evilblock.cubed.ScalaCommonsSpigot
import net.evilblock.cubed.util.CC
import org.bukkit.World
import org.bukkit.entity.Player
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.collections.set

/**
 * @author Subham
 * @since 7/19/25
 */
@Service
object HostedWorldInstanceService
{
    private val loadedWorldInstances = ConcurrentHashMap<String, HostedWorldInstance<*>>()
    private val loadedBukkitWorldInstancesMappings = ConcurrentHashMap<String, String>()

    private val players = ConcurrentHashMap<UUID, String>()
    private val pendingPlayerLogins = CacheBuilder
        .newBuilder()
        .expireAfterWrite(5L, TimeUnit.SECONDS)
        .build<UUID, String>()

    @Configure
    fun configure()
    {
        // Sanity unload checks
        Schedulers
            .async()
            .runRepeating({ _ ->
                loadedWorldInstances.values.forEach { instance ->
                    if (ScalaCommonsSpigot.instance.isAgonesInstanceDraining)
                    {
                        instance.state = HostedWorldState.DRAINING
                    }

                    if (instance.onlinePlayers().isEmpty())
                    {
                        if (instance.state == HostedWorldState.DECOMMISSIONING)
                        {
                            if (System.currentTimeMillis() - instance.initialTimeWhenEmpty >= 60_000L)
                            {
                                HostedWorldInstanceLifecycleController.unload(instance)
                                return@forEach
                            }
                        } else
                        {
                            if (instance.state == HostedWorldState.DRAINING)
                            {
                                // The instance is draining, it should be unavailable to
                                // players. So, we can unload eagerly
                                HostedWorldInstanceLifecycleController.unload(instance)
                                return@forEach
                            }

                            instance.initialTimeWhenEmpty = System.currentTimeMillis()
                            instance.state = HostedWorldState.DECOMMISSIONING
                        }
                    } else
                    {
                        if (instance.state != HostedWorldState.ACTIVE)
                        {
                            if (instance.state == HostedWorldState.DRAINING)
                            {
                                if (instance.drainStartTime == 0L)
                                {
                                    instance.drainStartTime = System.currentTimeMillis()
                                }

                                if (System.currentTimeMillis() - instance.drainStartTime >= Duration.ofMinutes(1L)
                                        .toMillis()
                                )
                                {
                                    HostedWorldInstanceLifecycleController.unload(instance)
                                    return@forEach
                                }

                                if (System.currentTimeMillis() - instance.lastDrainBroadcast >= Duration.ofSeconds(15L)
                                        .toMillis()
                                )
                                {
                                    instance.lastDrainBroadcast = System.currentTimeMillis()
                                    instance.bukkitWorld.players.forEach { player ->
                                        player.sendMessage("${CC.B_RED}YOUR WORLD IS REBOOTING")
                                        player.sendMessage("${CC.RED}You will be automatically disconnected in <1m, but you can join back instantly.")
                                    }
                                }
                                return@forEach
                            }

                            instance.state = HostedWorldState.ACTIVE
                        }
                    }

                    if (
                        instance.state == HostedWorldState.ACTIVE &&
                        instance.persistencePolicy == PersistencePolicy.PERSISTENT
                    )
                    {
                        val lastWorldSave = instance.loadedWorld.generic.getLastSave()
                            ?: System.currentTimeMillis()

                        if (System.currentTimeMillis() - lastWorldSave > 60_000L)
                        {
                            Versioned
                                .toProvider()
                                .getSlimeProvider()
                                .saveWorld(instance.loadedWorld.generic)
                        }
                    }
                }
            }, 0L, 20L)
    }

    fun linkPlayerToInstance(player: Player, instance: HostedWorldInstance<*>)
    {
        players[player.uniqueId] = instance.nameId
    }

    fun unlinkPlayer(player: Player)
    {
        players.remove(player.uniqueId)
    }

    fun registerWorldInstance(worldInstance: HostedWorldInstance<*>)
    {
        loadedWorldInstances[worldInstance.nameId] = worldInstance
        loadedBukkitWorldInstancesMappings[worldInstance.bukkitWorld.name] = worldInstance.nameId
    }

    fun unregisterWorldInstance(worldInstance: HostedWorldInstance<*>)
    {
        loadedWorldInstances.remove(worldInstance.nameId)
        loadedBukkitWorldInstancesMappings.remove(worldInstance.bukkitWorld.name)
    }

    fun ofWorld(world: World) = loadedBukkitWorldInstancesMappings[world.name]
        ?.let { worldId -> loadedWorldInstances[worldId] }

    fun instanceOf(player: Player) = players[player.uniqueId]
        ?.let {
            loadedWorldInstances[it]
        }

    fun instanceById(nameId: String) = loadedWorldInstances[nameId]
    fun instanceByGID(globalId: UUID) = loadedWorldInstances.values.firstOrNull { it.globalId == globalId }

    fun worldInstances() = loadedWorldInstances.values
    fun pendingLogin(player: UUID) = pendingPlayerLogins.getIfPresent(player)
    fun pendingLoginInstance(player: UUID) = pendingPlayerLogins.getIfPresent(player)
        ?.let { instanceById(it) }

    fun trackPendingLogin(player: UUID, world: HostedWorldInstance<*>)
    {
        pendingPlayerLogins.put(player, world.nameId)
    }

    fun removePendingLogin(player: UUID)
    {
        pendingPlayerLogins.invalidate(player)
    }
}
