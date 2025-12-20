package mc.arch.minigames.persistent.housing.game.actions.tasks.prevention

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import mc.arch.minigames.persistent.housing.api.action.option.TaskOption
import mc.arch.minigames.persistent.housing.api.action.tasks.Task
import mc.arch.minigames.persistent.housing.game.actions.HousingActionBukkitImplementation
import mc.arch.minigames.persistent.housing.game.translateCC
import net.md_5.bungee.api.ChatColor
import org.bukkit.event.block.BlockBreakEvent
import java.util.*

/**
 * Class created on 12/18/2025

 * @author Max C.
 * @project arch-minigames
 * @website https://solo.to/redis
 */
@Service
object PreventBlockBreakTask : Task(
    "preventBlockBreak",
    "Prevent Block Breaking",
    mutableMapOf(
        "shouldSendDenial" to TaskOption(
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

    override fun <E> apply(playerId: UUID?, event: E)
    {
        val blockBreakEvent = event as BlockBreakEvent
        val player = event.player

        blockBreakEvent.isCancelled = true

        if (option<Boolean>("shouldSendDenial"))
        {
            player.sendMessage(
               option<String>("denialMessage").translateCC()
            )
        }
    }
}