package mc.arch.minigames.persistent.housing.game.actions.events

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import mc.arch.minigames.persistent.housing.api.action.HousingActionService
import mc.arch.minigames.persistent.housing.api.action.player.ActionEvent
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerJoinEvent

/**
 * Class created on 12/18/2025

 * @author Max C.
 * @project arch-minigames
 * @website https://solo.to/redis
 */
@Service
object BlockPlaceActionEvent: ActionEvent
{
    override fun id(): String = "blockPlaceEvent"
    override fun eventClass(): Class<*> = BlockPlaceEvent::class.java

    @Configure
    fun configure()
    {
        HousingActionService.registerActionEvent(this)
    }
}