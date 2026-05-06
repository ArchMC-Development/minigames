package mc.arch.minigames.versioned.legacy

import mc.arch.minigames.versioned.generics.WorldProvider
import org.bukkit.World

/**
 * @author GrowlyX
 * @since 2026-05-06
 */
object LegacyWorldProvider : WorldProvider
{
    override fun setFeatureLightingEnabled(world: World, enabled: Boolean)
    {
        world.custom().worldConfig.FEATURES_LIGHTING_ENABLED = enabled
    }
}
