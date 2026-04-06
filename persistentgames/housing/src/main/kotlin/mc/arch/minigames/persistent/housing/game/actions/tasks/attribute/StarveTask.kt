package mc.arch.minigames.persistent.housing.game.actions.tasks.attribute

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import mc.arch.minigames.persistent.housing.api.action.tasks.Task
import mc.arch.minigames.persistent.housing.game.actions.HousingActionBukkitImplementation
import org.bukkit.Bukkit
import java.util.UUID

@Service
object StarveTask : Task(
    "starve",
    "Starve Player",
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
        player.foodLevel = 0
        player.saturation = 0.0f
    }
}
