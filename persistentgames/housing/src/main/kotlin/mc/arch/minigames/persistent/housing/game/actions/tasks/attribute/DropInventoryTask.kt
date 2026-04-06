package mc.arch.minigames.persistent.housing.game.actions.tasks.attribute

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import mc.arch.minigames.persistent.housing.api.action.tasks.Task
import mc.arch.minigames.persistent.housing.game.actions.HousingActionBukkitImplementation
import org.bukkit.Bukkit
import org.bukkit.Material
import java.util.UUID

@Service
object DropInventoryTask : Task(
    "dropInventory",
    "Drop Inventory",
    mutableMapOf()
)
{
    @Configure
    fun configure()
    {
        HousingActionBukkitImplementation.registerTask(this)
    }

    override fun <E> apply(playerId: UUID?, event: E)
    {
        val player = Bukkit.getPlayer(playerId) ?: return
        
        val saveItem = player.inventory.getItem(8)
        
        player.inventory.contents.filterNotNull()
            .filter { it.type != Material.AIR && it != saveItem }
            .forEach { item ->
                player.world.dropItemNaturally(player.location, item)
            }
            
        player.inventory.armorContents.filterNotNull()
            .filter { it.type != Material.AIR }
            .forEach { item ->
                player.world.dropItemNaturally(player.location, item)
            }

        player.inventory.clear()
        if (saveItem != null) {
            player.inventory.setItem(8, saveItem)
        }
        
        player.inventory.armorContents = null
        player.updateInventory()
    }
}
