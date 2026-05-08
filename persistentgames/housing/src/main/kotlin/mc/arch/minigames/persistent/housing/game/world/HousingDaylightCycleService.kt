package mc.arch.minigames.persistent.housing.game.world

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import mc.arch.minigames.persistent.housing.game.PersistentGameHousing
import me.lucko.helper.Events
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.event.world.WorldLoadEvent

@Service
object HousingDaylightCycleService
{
    @Configure
    fun configure()
    {
        for (world in Bukkit.getWorlds())
        {
            disableDaylightCycle(world)
        }

        Events.subscribe(WorldLoadEvent::class.java)
            .handler { disableDaylightCycle(it.world) }
            .bindWith(PersistentGameHousing.instance)
    }

    private fun disableDaylightCycle(world: World)
    {
        world.setGameRuleValue("doDaylightCycle", "false")
    }
}
