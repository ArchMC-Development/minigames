package mc.arch.minigames.persistent.housing.game.actions.tasks.prevention

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import mc.arch.minigames.persistent.housing.api.action.tasks.Task
import mc.arch.minigames.persistent.housing.game.actions.HousingActionBukkitImplementation
import mc.arch.minigames.persistent.housing.game.resources.getPlayerHouseFromInstance
import org.bukkit.Bukkit
import org.bukkit.event.Cancellable
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerPickupItemEvent
import java.util.UUID

@Service
object CancelEventTask : Task(
    "cancelEvent",
    "Cancel Event",
    mutableMapOf(),
    listOf(
        "blockBreakEvent",
        "blockPlaceEvent",
        "playerChatEvent",
        "playerCommandEvent",
        "playerDamageEvent",
        "playerDamageByPlayerEvent",
        "playerDropItemEvent",
        "playerFoodChangeEvent",
        "playerInteractEvent",
        "playerMoveEvent",
        "playerPickupItemEvent",
        "playerProjectileEvent"
    )
)
{
    @Configure
    fun configure()
    {
        HousingActionBukkitImplementation.registerTask(this)
    }

    override fun <E> apply(playerId: UUID?, event: E)
    {
        if (event !is Cancellable) {
            return
        }

        if (playerId != null && isAdminBypassable(event)) {
            val player = Bukkit.getPlayer(playerId)
            val house = player?.getPlayerHouseFromInstance()
            if (house != null && house.playerIsOrAboveAdministrator(playerId)) {
                return
            }
        }

        event.isCancelled = true
    }

    private fun isAdminBypassable(event: Any?): Boolean = event is BlockBreakEvent
        || event is BlockPlaceEvent
        || event is PlayerInteractEvent
        || event is PlayerDropItemEvent
        || event is PlayerPickupItemEvent
        || event is PlayerFishEvent
}
