package mc.arch.minigames.versioned.legacy

import mc.arch.minigames.versioned.generics.KnockbackProvider
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 2026-05-06
 */
object LegacyKnockbackProvider : KnockbackProvider
{
    override fun applyProfile(player: Player, group: String, profile: String)
    {
        val customProfile = Bukkit.custom()
            .knockbackManager.profileManager
            .getProfile(group, profile)
            ?: return

        customProfile.setKnockback(player)
    }
}
