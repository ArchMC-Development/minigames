package mc.arch.minigames.versioned.modern

import mc.arch.minigames.versioned.generics.KnockbackProvider
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 2026-05-06
 *
 * Custom knockback profiles are a 1.8 Spigot-fork concept and don't apply on modern Paper.
 * Vanilla 1.21 knockback is tuned via the GENERIC_KNOCKBACK_RESISTANCE attribute and combat-
 * mechanic flags, not named profiles. No-op on modern.
 */
object ModernKnockbackProvider : KnockbackProvider
{
    override fun applyProfile(player: Player, group: String, profile: String)
    {
    }
}
