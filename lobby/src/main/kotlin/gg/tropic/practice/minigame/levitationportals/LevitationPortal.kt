package gg.tropic.practice.minigame.levitationportals

import gg.scala.commons.spatial.toPosition
import gg.tropic.practice.configuration.minigame.levitationportal.LevitationPortalSpec
import me.lucko.helper.Events
import me.lucko.helper.Helper
import me.lucko.helper.Schedulers
import me.lucko.helper.terminable.composite.CompositeTerminable
import net.evilblock.cubed.visibility.VisibilityHandler
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.util.Vector

/**
 * @author GrowlyX
 * @since 7/26/2022
 */
class LevitationPortal(
    private val spec: LevitationPortalSpec,
    private val portalEnter: (Player) -> Unit = {},
    private val portalExit: (Player) -> Unit = {},
    private val portalTeleport: (Player) -> Unit = {},
)
{
    fun subscribe(terminable: CompositeTerminable)
    {
        Events.subscribe(PlayerQuitEvent::class.java)
            .handler {
                it.player.removeSession()
            }
            .bindWith(terminable)

        Events.subscribe(PlayerMoveEvent::class.java)
            .filter {
                it.player.gameMode != GameMode.CREATIVE
            }
            .handler {
                val player = it.player
                val session = player.getSession()
                val withinCuboid = spec.bounds.contains(player.location.toPosition())

                if (!withinCuboid && session != null)
                {
                    player.removeSession()
                    portalExit.invoke(player)
                    VisibilityHandler.updateToAll(player)

                    player.isFlying = false
                    player.allowFlight = false
                    return@handler
                }

                val location = player.location
                if (withinCuboid && session == null)
                {
                    if (spec.restrictBelowAir)
                    {
                        val block = location.block.location.clone()
                            .add(0.0, -1.0, 0.0).block

                        if (block.type != Material.AIR)
                        {
                            return@handler
                        }
                    }

                    player.setSession(
                        LevitationSession(
                            portalId = spec.id,
                            location = location.clone(),
                            incremental = 1,
                            terminable = CompositeTerminable.create()
                        )
                    )

                    val floaterInfo = player.getSession()!!

                    this.portalEnter.invoke(player)
                    VisibilityHandler.updateToAll(player)

                    Schedulers.sync()
                        .runRepeating({ task ->
                            if (!player.isOnline)
                            {
                                player.removeSession()
                                task.closeAndReportException()
                                return@runRepeating
                            }

                            if (floaterInfo.incremental <= 3)
                            {
                                player.velocity = Vector(
                                    player.velocity.x + .03,
                                    player.velocity.y * .06,
                                    player.velocity.z
                                )

                                player.velocity = player.location.direction
                                    .multiply(0.19)
                                    .setY(0.01)

                                if (floaterInfo.incremental == 3)
                                {
                                    player.isFlying = false
                                    task.closeAndReportException()
                                    return@runRepeating
                                }

                                floaterInfo.incremental += 1
                            }
                        }, 0L, 1L)
                        .bindWith(floaterInfo.terminable)

                    var increment = 0.03
                    var handledTeleportationLogic = false

                    Schedulers.sync()
                        .runRepeating({ task ->
                            if (!player.isOnline)
                            {
                                player.removeSession()
                                task.closeAndReportException()
                                return@runRepeating
                            }

                            val currentLocation = player.location

                            if (
                                currentLocation.y >= location.y + spec.height &&
                                !handledTeleportationLogic
                            )
                            {
                                handledTeleportationLogic = true
                                this.portalTeleport.invoke(player)
                                return@runRepeating
                            }

                            player.velocity = currentLocation
                                .direction.multiply(0.12)
                                .setY(increment)

                            if (increment <= spec.limit)
                            {
                                increment += .005
                            }
                        }, 3L, 1L)
                        .bindWith(floaterInfo.terminable)
                }
            }
            .bindWith(terminable)
    }
}
