package mc.arch.minigames.persistent.housing.game.actions.tasks.prevention

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import mc.arch.minigames.persistent.housing.api.action.option.TaskOption
import mc.arch.minigames.persistent.housing.api.action.tasks.Task
import mc.arch.minigames.persistent.housing.game.actions.HousingActionBukkitImplementation
import org.bukkit.Bukkit
import org.bukkit.event.block.BlockBreakEvent
import java.util.*

/**
 * Class created on 12/18/2025

 * @author Max C.
 * @project arch-minigames
 * @website https://solo.to/redis
 */
@Service
object PreventBlockBreakTask : Task<BlockBreakEvent>(
    "preventBlockBreak",
    "Prevent Block Breaking",
    mutableMapOf(
        "shouldSendDential" to TaskOption(
            "Send Denial Message?",
            "false"
        ),
        "denialMessage" to
            TaskOption(
                "Dential Message",
                "&cYou cannot do this here!"
            )
    )
)
{
    @Configure
    fun configure()
    {
        HousingActionBukkitImplementation.registerTask(this)
    }

    override fun apply(playerId: UUID?, event: BlockBreakEvent)
    {
        val player = event.player

        event.isCancelled = true
    }
}