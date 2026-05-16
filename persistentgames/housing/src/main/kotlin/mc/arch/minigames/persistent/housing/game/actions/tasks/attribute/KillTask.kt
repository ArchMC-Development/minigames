package mc.arch.minigames.persistent.housing.game.actions.tasks.attribute

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import mc.arch.minigames.persistent.housing.api.action.tasks.Task
import mc.arch.minigames.persistent.housing.game.actions.HousingActionBukkitImplementation
import org.bukkit.Bukkit
import org.bukkit.event.entity.PlayerDeathEvent
import java.util.UUID

@Service
object KillTask : Task(
    "kill",
    "Kill Player",
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
        if (event is PlayerDeathEvent) return
        val player = Bukkit.getPlayer(playerId) ?: return
        if (player.isDead || player.health <= 0.0) return
        player.health = 0.0
    }
}
