package mc.arch.minigames.versioned.legacy

import mc.arch.minigames.versioned.generics.PlayerProvider
import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 8/6/25
 */
object LegacyPlayerProvider : PlayerProvider
{
    override fun obfuscateHealth(player: Player, obfuscated: Boolean)
    {
        //player.custom().tracker.isHealthObfuscated = obfuscated
    }
}
