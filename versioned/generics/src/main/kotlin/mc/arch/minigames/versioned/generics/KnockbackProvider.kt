package mc.arch.minigames.versioned.generics

import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 2026-05-06
 */
interface KnockbackProvider
{
    fun applyProfile(player: Player, group: String, profile: String)
}
