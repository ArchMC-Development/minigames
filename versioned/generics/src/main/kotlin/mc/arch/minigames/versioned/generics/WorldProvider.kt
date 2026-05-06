package mc.arch.minigames.versioned.generics

import org.bukkit.World

/**
 * @author GrowlyX
 * @since 2026-05-06
 */
interface WorldProvider
{
    fun setFeatureLightingEnabled(world: World, enabled: Boolean)
}
