package mc.arch.minigames.persistent.housing.api.action.events

interface ActionEvent
{
    fun id(): String
    fun eventClass(): Class<*>
}