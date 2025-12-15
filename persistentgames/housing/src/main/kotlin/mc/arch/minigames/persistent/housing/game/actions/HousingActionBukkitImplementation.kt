package mc.arch.minigames.persistent.housing.game.actions

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import mc.arch.minigames.persistent.housing.api.action.tasks.Task

@Service
object HousingActionBukkitImplementation
{
    private var tasks: MutableMap<String, Task> = mutableMapOf()

    @Configure
    fun configure()
    {

    }

    fun registerTask(task: Task)
    {
        tasks[task.id] = task
    }
}