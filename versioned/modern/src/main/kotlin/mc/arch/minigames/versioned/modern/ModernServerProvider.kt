package mc.arch.minigames.versioned.modern

import mc.arch.minigames.versioned.generics.ServerProvider
import org.bukkit.Bukkit

/**
 * @author GrowlyX
 * @since 2026-05-06
 */
object ModernServerProvider : ServerProvider
{
    override fun currentTick(): Int = Bukkit.getCurrentTick()
}
