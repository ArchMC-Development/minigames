package mc.arch.minigames.persistent.housing.game.actions.tasks.attribute

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import mc.arch.minigames.persistent.housing.api.action.option.TaskOption
import mc.arch.minigames.persistent.housing.api.action.tasks.Task
import mc.arch.minigames.persistent.housing.game.actions.HousingActionBukkitImplementation
import mc.arch.minigames.persistent.housing.game.translateCC
import org.bukkit.Bukkit
import java.util.UUID

/**
 * Class created on 12/23/2025

 * @author Max C.
 * @project arch-minigames
 * @website https://solo.to/redis
 */
@Service
object FullHealTask : Task(
    "fullHeal",
    "Full Heal",
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
        val player = Bukkit.getPlayer(playerId)
            ?: return

        player.health = 20.0
    }
}