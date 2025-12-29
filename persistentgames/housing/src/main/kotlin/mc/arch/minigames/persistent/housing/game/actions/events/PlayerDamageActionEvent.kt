package mc.arch.minigames.persistent.housing.game.actions.events

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import mc.arch.minigames.persistent.housing.api.action.HousingActionService
import mc.arch.minigames.persistent.housing.api.action.player.ActionEvent
import net.evilblock.cubed.entity.living.event.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent

@Service
object PlayerDamageActionEvent: ActionEvent
{
    override fun id(): String = "playerDamageEvent"
    override fun eventClass(): Class<*> = EntityDamageEvent::class.java

    @Configure
    fun configure()
    {
        HousingActionService.registerActionEvent(this)
    }
}
