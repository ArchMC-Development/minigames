package mc.arch.minigames.persistent.housing.api.action.tasks

interface Task
{
    fun id(): String
    fun displayName(): String

    fun apply()
}