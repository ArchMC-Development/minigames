package gg.solara.practice.editor

import me.lucko.helper.terminable.Terminable
import org.bukkit.Bukkit
import org.bukkit.World

/**
 * @author GrowlyX
 * @since 7/18/2024
 */
data class EditorInstance(
    val associatedEditable: Editable,
    val world: World
): Terminable
{
    override fun close()
    {
        if (Bukkit.getWorld(world.name) == null)
        {
            return
        }

        Bukkit.unloadWorld(world, false)
    }
}
