package mc.arch.minigames.versioned.modern

import mc.arch.minigames.versioned.generics.PlayerProvider
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.Fireball
import org.bukkit.entity.Player
import org.bukkit.entity.TNTPrimed
import org.bukkit.util.Vector

/**
 * @author Subham
 * @since 8/6/25
 */
object ModernPlayerProvider : PlayerProvider
{
    override fun obfuscateHealth(player: Player, obfuscated: Boolean)
    {
    }

    override fun mountImmovable(player: Player): Int =
        spawnSeat(player, player.location)

    override fun mountImmovable(player: Player, location: Location): Int =
        spawnSeat(player, location)

    private fun spawnSeat(player: Player, location: Location): Int
    {
        val seat = location.world.spawn(
            location.clone().add(0.0, -1.7, 0.0),
            ArmorStand::class.java
        ) { stand ->
            stand.isInvisible = true
            stand.isInvulnerable = true
            stand.setGravity(false)
            stand.isMarker = true
            stand.setAI(false)
        }
        seat.addPassenger(player)
        return seat.entityId
    }

    override fun unmount(player: Player, mountId: Int)
    {
        val entity = player.world.entities.firstOrNull { it.entityId == mountId }
        if (entity != null)
        {
            entity.removePassenger(player)
            entity.remove()
        }
    }

    override fun hideNametag(entity: Entity)
    {
        entity.isCustomNameVisible = false
    }

    override fun setSurvivalGameMode(player: Player)
    {
        player.gameMode = GameMode.SURVIVAL
    }

    override fun freezeTime(player: Player, frozen: Boolean)
    {
        if (frozen)
        {
            player.setPlayerTime(player.world.fullTime, false)
        } else
        {
            player.resetPlayerTime()
        }
    }

    override fun setFireballDirection(fireball: Fireball, direction: Vector): Fireball
    {
        fireball.direction = direction
        return fireball
    }

    override fun setTntSource(tnt: TNTPrimed, owner: Player)
    {
        tnt.source = owner
    }
}
