package mc.arch.minigames.persistent.housing.game.actions.tasks.display

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import mc.arch.minigames.persistent.housing.api.action.option.TaskOption
import mc.arch.minigames.persistent.housing.api.action.tasks.Task
import mc.arch.minigames.persistent.housing.api.action.util.HousingActionPrimitive
import mc.arch.minigames.persistent.housing.game.actions.HousingActionBukkitImplementation
import mc.arch.minigames.persistent.housing.game.getReference
import mc.arch.minigames.persistent.housing.game.resources.getPlayerHouseFromInstance
import mc.arch.minigames.persistent.housing.game.translateCC
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerJoinEvent
import java.util.UUID

@Service
object BroadcastMessageTask : Task(
    "broadcastMessage",
    "Broadcast Message",
    mutableMapOf(
        "message" to
            TaskOption(
                "Message",
                "This is a default message!",
                HousingActionPrimitive.STRING
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
        val player = Bukkit.getPlayer(playerId)
            ?: return
        val house = player.getPlayerHouseFromInstance()
            ?: return
        val reference = house.getReference()
            ?: return

        reference.onlinePlayers.mapNotNull { Bukkit.getPlayer(it) }
            .forEach {
                it.sendMessage(option<String>("message").translateCC())
            }
    }
}