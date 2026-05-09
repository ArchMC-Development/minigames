package gg.solara.practice.editor

import gg.tropic.practice.versioned.Versioned
import org.bukkit.World
import java.util.UUID

/**
 * @author GrowlyX
 * @since 7/18/2024
 */
object EditorGenerator
{
    fun createNewEmptyWorld(): World =
        Versioned.toProvider()
            .getSchematicProvider()
            .createEmptyVoidWorld("editor_temp_${UUID.randomUUID()}")
}
