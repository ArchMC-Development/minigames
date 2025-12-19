package mc.arch.minigames.persistent.housing.game.actions

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import mc.arch.minigames.persistent.housing.api.action.tasks.Task
import mc.arch.minigames.persistent.housing.game.resources.getPlayerHouseFromInstance
import me.lucko.helper.Events
import me.lucko.helper.terminable.composite.CompositeTerminable
import org.bukkit.event.block.BlockBreakEvent

@Service
object HousingActionBukkitImplementation
{
    private var tasks: MutableMap<String, Task<*>> = mutableMapOf()
    private val terminable: CompositeTerminable = CompositeTerminable.create()

    @Configure
    fun configure()
    {
        Events.subscribe(BlockBreakEvent::class.java)
            .handler { event ->
                val player = event.player
                val house = player.getPlayerHouseFromInstance()

                if (house == null)
                {
                    event.isCancelled = true
                    return@handler
                }


            }.bindWith(terminable)
    }

    fun registerTask(task: Task<*>)
    {
        tasks[task.id] = task
    }
}