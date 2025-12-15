package mc.arch.minigames.persistent.housing.game.actions.tasks

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import mc.arch.minigames.persistent.housing.api.action.option.TaskOption
import mc.arch.minigames.persistent.housing.api.action.tasks.Task
import mc.arch.minigames.persistent.housing.game.actions.HousingActionBukkitImplementation
import org.bukkit.Bukkit
import java.util.UUID

@Service
object SendMessageTask : Task(
    "sendMessage",
    "Send Message",
    mutableMapOf(
        "message" to
            TaskOption(
                "Message",
                "This is a default message!"
            )
    )
)
{
    @Configure
    fun configure()
    {
        HousingActionBukkitImplementation.registerTask(this)
    }

    override fun apply(playerId: UUID)
    {
        val player = Bukkit.getPlayer(playerId)
            ?: return

        player.sendMessage(option<String>("message"))
    }
}