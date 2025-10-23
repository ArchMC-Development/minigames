package mc.arch.minigames.versioned.generics.worlds

import mc.arch.minigames.versioned.generics.SlimeWorldGeneric
import org.bukkit.World

/**
 * @author Subham
 * @since 8/23/25
 */
data class LoadedSlimeWorld(
    val bukkitWorld: World,
    val generic: SlimeWorldGeneric<*>
)
