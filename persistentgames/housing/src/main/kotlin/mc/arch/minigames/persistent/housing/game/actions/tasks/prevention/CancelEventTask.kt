package mc.arch.minigames.persistent.housing.game.actions.tasks.prevention

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import mc.arch.minigames.persistent.housing.api.action.tasks.Task
import mc.arch.minigames.persistent.housing.game.actions.HousingActionBukkitImplementation
import org.bukkit.event.Cancellable
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
        if (event is Cancellable) {
            event.isCancelled = true
        }
    }
}
