package mc.arch.minigames.persistent.housing.game.death

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import mc.arch.minigames.persistent.housing.game.resources.getPlayerHouseFromInstance
import mc.arch.minigames.persistent.housing.game.spatial.toLocation
import me.lucko.helper.Events
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerRespawnEvent

@Service
object HousingDeathHandler
{
    @Configure
    fun configure()
    {
        Events.subscribe(PlayerDeathEvent::class.java)
            .handler { event ->
                val player = event.entity
                player.getPlayerHouseFromInstance() ?: return@handler

                Tasks.delayed(1L) {
                    player.spigot().respawn()
                }
            }

        Events.subscribe(PlayerRespawnEvent::class.java)
            .handler { event ->
                val player = event.player
                val house = player.getPlayerHouseFromInstance() ?: return@handler
                val spawnPoint = house.spawnPoint ?: return@handler

                event.respawnLocation = spawnPoint.toLocation(player.world)
            }
    }
}
