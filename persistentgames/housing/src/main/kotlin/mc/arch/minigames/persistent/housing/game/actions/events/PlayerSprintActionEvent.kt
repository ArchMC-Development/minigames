package mc.arch.minigames.persistent.housing.game.actions.events

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import mc.arch.minigames.persistent.housing.api.action.HousingActionService
import mc.arch.minigames.persistent.housing.api.action.player.ActionEvent
import org.bukkit.event.player.PlayerToggleSprintEvent

@Service
object PlayerSprintActionEvent: ActionEvent
{
    override fun id(): String = "playerSprintEvent"
    override fun eventClass(): Class<*> = PlayerToggleSprintEvent::class.java

    @Configure
    fun configure()
    {
        HousingActionService.registerActionEvent(this)
    }
}
