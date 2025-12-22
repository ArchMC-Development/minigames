package mc.arch.minigames.persistent.housing.api.action

import gg.scala.flavor.service.Service
import mc.arch.minigames.persistent.housing.api.action.player.ActionEvent

@Service
object HousingActionService
{
    private val events: MutableMap<String, ActionEvent> = mutableMapOf()

    fun getAllEvents(): List<ActionEvent> = events.values.toList()

    fun getByName(name: String): ActionEvent? = events[name]

    fun registerActionEvent(event: ActionEvent)
    {
        events[event.id()] = event
    }
}