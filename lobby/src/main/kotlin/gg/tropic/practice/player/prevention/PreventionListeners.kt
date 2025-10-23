package gg.tropic.practice.player.prevention

import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.tropic.practice.PracticeLobby
import gg.tropic.practice.configuration.PracticeConfigurationService
import gg.tropic.practice.commands.menu.admin.prevention.AllowRemoveItemsWithinInventory
import me.lucko.helper.Events
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.bukkit.ItemUtils
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.*
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.*

@Service
object PreventionListeners
{
    @Inject
    lateinit var plugin: PracticeLobby

    private fun hasBuilderMetadata(player: Player) = player.hasMetadata("builder")

    @Configure
    fun configure()
    {
        Events.subscribe(PlayerItemConsumeEvent::class.java)
            .handler { event ->
                if (hasBuilderMetadata(event.player)) return@handler
                event.isCancelled = true
            }
            .bindWith(plugin)

        Events.subscribe(PlayerInteractEvent::class.java)
            .handler { event ->
                if (hasBuilderMetadata(event.player)) return@handler
                if (event.clickedBlock != null)
                {
                    if (event.clickedBlock.type.name.contains("CHEST"))
                    {
                        event.isCancelled = true
                    }
                }
            }
            .bindWith(plugin)

        Events.subscribe(PlayerBucketEmptyEvent::class.java)
            .handler { event ->
                if (hasBuilderMetadata(event.player)) return@handler
                event.isCancelled = true
            }
            .bindWith(plugin)

        Events.subscribe(PlayerBucketFillEvent::class.java)
            .handler { event ->
                if (hasBuilderMetadata(event.player)) return@handler
                event.isCancelled = true
            }
            .bindWith(plugin)

        Events.subscribe(ProjectileLaunchEvent::class.java)
            .handler { event ->
                if (event.entity.shooter is Player && hasBuilderMetadata(event.entity.shooter as Player)) return@handler
                event.isCancelled = true
            }
            .bindWith(plugin)

        Events.subscribe(PlayerInteractEvent::class.java)
            .handler {
                if (hasBuilderMetadata(it.player)) return@handler
                it.setUseInteractedBlock(Event.Result.DENY)
            }
            .bindWith(plugin)

        Events.subscribe(PlayerPickupItemEvent::class.java)
            .handler {
                if (hasBuilderMetadata(it.player)) return@handler
                it.isCancelled = true
            }
            .bindWith(plugin)

        Events.subscribe(PlayerDropItemEvent::class.java)
            .handler {
                if (hasBuilderMetadata(it.player)) return@handler
                val notContentEditor = Menu.currentlyOpenedMenus[it.player.uniqueId] !is AllowRemoveItemsWithinInventory
                if (notContentEditor)
                {
                    it.isCancelled = true
                    return@handler
                }
                it.itemDrop.remove()
            }
            .bindWith(plugin)

        Events.subscribe(InventoryClickEvent::class.java)
            .filter {
                it.clickedInventory == it.whoClicked.inventory &&
                    (it.slotType == InventoryType.SlotType.ARMOR ||
                        (runCatching { ItemUtils.itemTagHasKey(it.currentItem, "invokerc") }.getOrDefault(false)))
            }
            .handler {
                if (hasBuilderMetadata(it.whoClicked as Player)) return@handler
                it.isCancelled = true
                it.cursor = null
            }
            .bindWith(plugin)

        Events.subscribe(InventoryClickEvent::class.java)
            .filter {
                it.clickedInventory == it.whoClicked.inventory && it.click.isKeyboardClick
            }
            .handler {
                if (hasBuilderMetadata(it.whoClicked as Player)) return@handler
                it.isCancelled = Menu.currentlyOpenedMenus[it.inventory.viewers.first().uniqueId] !is AllowRemoveItemsWithinInventory
            }
            .bindWith(plugin)

        Events.subscribe(InventoryDragEvent::class.java)
            .handler {
                if (hasBuilderMetadata(it.viewers.first() as Player)) return@handler
                val notContentEditor = Menu.currentlyOpenedMenus[it.viewers.first().uniqueId] !is AllowRemoveItemsWithinInventory
                if (notContentEditor)
                {
                    it.isCancelled = true
                    return@handler
                }
            }
            .bindWith(plugin)

        Events.subscribe(InventoryMoveItemEvent::class.java)
            .handler {
                if (hasBuilderMetadata(it.initiator.viewers.first() as Player)) return@handler
                val notContentEditor = Menu.currentlyOpenedMenus[it.initiator.viewers.first().uniqueId] !is AllowRemoveItemsWithinInventory
                if (notContentEditor)
                {
                    it.isCancelled = true
                    return@handler
                }
            }
            .bindWith(plugin)

        listOf(EntityDamageEvent::class, EntityDamageByBlockEvent::class, EntityDamageByEntityEvent::class)
            .forEach { damageEvent ->
                Events.subscribe(damageEvent.java)
                    .handler {
                        if (it.entity is Player && hasBuilderMetadata(it.entity as Player)) return@handler
                        if (it.cause == EntityDamageEvent.DamageCause.VOID)
                        {
                            it.entity.teleport(
                                PracticeConfigurationService.local()
                                    .spawnLocation
                                    .toLocation(it.entity.world)
                            )
                        }
                        it.isCancelled = true
                    }
                    .bindWith(plugin)
            }

        listOf(BlockPlaceEvent::class, BlockBreakEvent::class, FoodLevelChangeEvent::class)
            .forEach { event ->
                Events.subscribe(event.java)
                    .handler {
                        if (it is PlayerEvent && hasBuilderMetadata(it.player)) return@handler
                        it.isCancelled = true
                    }
                    .bindWith(plugin)
            }
    }
}
