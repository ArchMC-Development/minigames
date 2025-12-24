package mc.arch.minigames.persistent.housing.game.actions.events

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import mc.arch.minigames.persistent.housing.api.action.HousingActionService
import mc.arch.minigames.persistent.housing.api.action.player.ActionEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

@Service
object PlayerDeathActionEvent: ActionEvent
{
    override fun id(): String = "playerDeathEvent"
    override fun eventClass(): Class<*> = PlayerDeathEvent::class.java

    @Configure
    fun configure()
    {
        HousingActionService.registerActionEvent(this)
    }
}