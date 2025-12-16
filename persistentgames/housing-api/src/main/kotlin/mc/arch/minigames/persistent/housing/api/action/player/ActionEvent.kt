package mc.arch.minigames.persistent.housing.api.action.player

interface ActionEvent
{
    fun id(): String
    fun eventClass(): Class<*>
}