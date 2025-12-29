package mc.arch.minigames.persistent.housing.game.actions.events

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import mc.arch.minigames.persistent.housing.api.action.HousingActionService
import mc.arch.minigames.persistent.housing.api.action.player.ActionEvent
import org.bukkit.event.player.PlayerFishEvent

@Service
object PlayerFishActionEvent: ActionEvent
{
    override fun id(): String = "playerFishEvent"
    override fun eventClass(): Class<*> = PlayerFishEvent::class.java

    @Configure
    fun configure()
    {
        HousingActionService.registerActionEvent(this)
    }
}
