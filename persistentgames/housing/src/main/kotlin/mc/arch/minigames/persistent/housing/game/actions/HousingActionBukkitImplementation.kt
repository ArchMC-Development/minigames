package mc.arch.minigames.persistent.housing.game.actions

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import mc.arch.minigames.persistent.housing.api.action.tasks.Task
import mc.arch.minigames.persistent.housing.game.resources.getPlayerHouseFromInstance
import me.lucko.helper.Events
import me.lucko.helper.terminable.composite.CompositeTerminable
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerPickupItemEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.event.player.PlayerToggleSprintEvent

@Service
object HousingActionBukkitImplementation
{
    private var tasks: MutableMap<String, Task> = mutableMapOf()
    private val terminable: CompositeTerminable = CompositeTerminable.create()

    @Configure
    fun configure()
    {
        // Block events
        subscribePlayerEvent<BlockBreakEvent>(BlockBreakEvent::class.java) { it.player }
        subscribePlayerEvent<BlockPlaceEvent>(BlockPlaceEvent::class.java) { it.player }

        // Player movement/position events
        subscribePlayerEvent<PlayerMoveEvent>(PlayerMoveEvent::class.java) { it.player }
        subscribePlayerEvent<PlayerTeleportEvent>(PlayerTeleportEvent::class.java) { it.player }
        subscribePlayerEvent<PlayerToggleSneakEvent>(PlayerToggleSneakEvent::class.java) { it.player }
        subscribePlayerEvent<PlayerToggleSprintEvent>(PlayerToggleSprintEvent::class.java) { it.player }

        // Player lifecycle events
        subscribePlayerEvent<PlayerJoinEvent>(PlayerJoinEvent::class.java) { it.player }
        subscribePlayerEvent<PlayerQuitEvent>(PlayerQuitEvent::class.java) { it.player }
        subscribePlayerEvent<PlayerRespawnEvent>(PlayerRespawnEvent::class.java) { it.player }
        subscribePlayerEvent<PlayerDeathEvent>(PlayerDeathEvent::class.java) { it.entity }

        // Player interaction events
        subscribePlayerEvent<PlayerInteractEvent>(PlayerInteractEvent::class.java) { it.player }
        subscribePlayerEvent<PlayerDropItemEvent>(PlayerDropItemEvent::class.java) { it.player }
        subscribePlayerEvent<PlayerPickupItemEvent>(PlayerPickupItemEvent::class.java) { it.player }
        subscribePlayerEvent<PlayerFishEvent>(PlayerFishEvent::class.java) { it.player }

        // Chat and command events
        subscribePlayerEvent<AsyncPlayerChatEvent>(AsyncPlayerChatEvent::class.java) { it.player }
        subscribePlayerEvent<PlayerCommandPreprocessEvent>(PlayerCommandPreprocessEvent::class.java) { it.player }

        // Food change event
        Events.subscribe(FoodLevelChangeEvent::class.java)
            .handler { event ->
                val player = event.entity as? Player
                    ?: return@handler

                val house = player.getPlayerHouseFromInstance()
                    ?: return@handler

                house.getAllActionEventsBy(FoodLevelChangeEvent::class.java)
                    .forEach { entry ->
                        entry.value.map { it.asRegistered() }.forEach { action ->
                            action.apply(player.uniqueId, event)
                        }
                    }
            }.bindWith(terminable)

        // Entity damage event (generic - any damage)
        Events.subscribe(EntityDamageEvent::class.java)
            .filter { it.entity is Player }
            .handler { event ->
                val player = event.entity as Player
                val house = player.getPlayerHouseFromInstance()
                    ?: return@handler

                house.getAllActionEventsBy(EntityDamageEvent::class.java)
                    .forEach { entry ->
                        entry.value.map { it.asRegistered() }.forEach { action ->
                            action.apply(player.uniqueId, event)
                        }
                    }
            }.bindWith(terminable)

        // Entity damage by entity event (PvP)
        Events.subscribe(EntityDamageByEntityEvent::class.java)
            .filter { it.entity is Player && it.damager is Player }
            .handler { event ->
                val player = event.entity as Player
                val house = player.getPlayerHouseFromInstance()
                    ?: return@handler

                house.getAllActionEventsBy(EntityDamageByEntityEvent::class.java)
                    .forEach { entry ->
                        entry.value.map { it.asRegistered() }.forEach { action ->
                            action.apply(player.uniqueId, event)
                        }
                    }
            }.bindWith(terminable)

        // Projectile launch event
        Events.subscribe(ProjectileLaunchEvent::class.java)
            .filter { it.entity.shooter is Player }
            .handler { event ->
                val player = event.entity.shooter as Player
                val house = player.getPlayerHouseFromInstance()
                    ?: return@handler

                house.getAllActionEventsBy(ProjectileLaunchEvent::class.java)
                    .forEach { entry ->
                        entry.value.map { it.asRegistered() }.forEach { action ->
                            action.apply(player.uniqueId, event)
                        }
                    }
            }.bindWith(terminable)
    }

    /**
     * Helper to subscribe a Bukkit event that has a direct player accessor.
     */
    private inline fun <reified T : org.bukkit.event.Event> subscribePlayerEvent(
        eventClass: Class<T>,
        crossinline playerExtractor: (T) -> Player
    )
    {
        Events.subscribe(eventClass)
            .handler { event ->
                val player = playerExtractor(event)
                val house = player.getPlayerHouseFromInstance()
                    ?: return@handler

                house.getAllActionEventsBy(eventClass)
                    .forEach { entry ->
                        entry.value.map { it.asRegistered() }.forEach { action ->
                            action.apply(player.uniqueId, event)
                        }
                    }
            }.bindWith(terminable)
    }

    fun getAllTasks() = tasks.values.toList()

    fun registerTask(task: Task)
    {
        tasks[task.id] = task
    }
}
