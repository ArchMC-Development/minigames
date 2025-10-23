package gg.tropic.practice.games.robot

import me.lucko.helper.terminable.composite.CompositeTerminable
import org.bukkit.entity.Player
import java.util.UUID

/**
 * @author GrowlyX
 * @since 7/20/2024
 */
abstract class RobotInstance
{
    val terminable = CompositeTerminable.create()
    var hasInitialSpawned = false

    abstract fun uniqueID(): UUID
    abstract fun solaraID(): UUID

    abstract fun name(): String
    abstract fun level(): String
    abstract fun ping(): Int

    abstract fun alive(): Boolean
    abstract fun initialSpawn()

    abstract fun participantConnected(player: Player)
    abstract fun spectatorConnected(player: Player)

    abstract fun destroy()
}
