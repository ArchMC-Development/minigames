package gg.tropic.practice.menu

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import me.lucko.helper.Events
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Service
object InventoryEventsListener
{
    val inventoryMap = ConcurrentHashMap<UUID, BukkitInventoryCallback>()

    @Configure
    fun configure()
    {
        Events
            .subscribe(InventoryClickEvent::class.java)
            .filter { it.whoClicked is Player }
            .filter { inventoryMap.containsKey(it.whoClicked.uniqueId) }
            .handler { event ->
                val callback = inventoryMap[event.whoClicked.uniqueId]
                    ?: return@handler

                val player = event.whoClicked as Player

                // Check if this is a button click
                val button = callback.buttons[event.rawSlot]
                if (button != null)
                {
                    event.isCancelled = true
                    button.onClick(player)
                    return@handler
                }

                // Block clicks on immutable slots
                if (event.rawSlot in callback.immutableSlots)
                {
                    event.isCancelled = true
                }
            }

        Events
            .subscribe(InventoryCloseEvent::class.java)
            .filter { it.player is Player }
            .filter { inventoryMap.containsKey(it.player.uniqueId) }
            .handler { event ->
                val callback = inventoryMap.remove(event.player.uniqueId)
                    ?: return@handler

                if (!callback.skipCloseCallback)
                {
                    callback.callback?.invoke(event.inventory.contents)
                }
            }
    }
}
