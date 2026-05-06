package mc.arch.minigames.versioned.generics

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Fireball
import org.bukkit.entity.Player
import org.bukkit.entity.TNTPrimed
import org.bukkit.util.Vector

/**
 * @author Subham
 * @since 8/6/25
 */
interface PlayerProvider
{
    fun obfuscateHealth(player: Player, obfuscated: Boolean)

    fun mountImmovable(player: Player): Int
    fun mountImmovable(player: Player, location: Location): Int
    fun unmount(player: Player, mountId: Int)

    fun hideNametag(entity: Entity)

    fun setSurvivalGameMode(player: Player)

    fun freezeTime(player: Player, frozen: Boolean)

    fun setFireballDirection(fireball: Fireball, direction: Vector): Fireball

    fun setTntSource(tnt: TNTPrimed, owner: Player)
}
