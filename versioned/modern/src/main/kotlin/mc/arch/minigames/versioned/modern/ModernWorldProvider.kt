package mc.arch.minigames.versioned.modern

import mc.arch.minigames.versioned.generics.WorldProvider
import org.bukkit.World

/**
 * @author GrowlyX
 * @since 2026-05-06
 *
 * The custom Spigot-fork lighting toggle has no Paper-side equivalent. Paper 1.21
 * computes lighting natively without the per-feature flag, so this is a no-op.
 */
object ModernWorldProvider : WorldProvider
{
    override fun setFeatureLightingEnabled(world: World, enabled: Boolean)
    {
    }
}
