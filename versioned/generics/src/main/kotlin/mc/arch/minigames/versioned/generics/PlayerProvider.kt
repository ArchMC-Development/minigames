package mc.arch.minigames.versioned.generics

import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 8/6/25
 */
interface PlayerProvider
{
    fun obfuscateHealth(player: Player, obfuscated: Boolean)
}
