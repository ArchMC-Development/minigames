package gg.solara.practice.editor

import com.cryptomorin.xseries.XMaterial
import com.sk89q.worldedit.Vector
import gg.solara.practice.utilities.WorldEditUtils
import org.bukkit.World
import org.bukkit.entity.Player
import java.io.File

/**
 * @author Subham
 * @since 6/24/25
 */
class SchematicEditable(private val file: File) : Editable
{
    override val icon: XMaterial
        get() = XMaterial.PAPER
    override val displayName: String
        get() = file.name.split(".").first()

    override fun prepareWorld(player: Player): World
    {
        val newWorld = EditorGenerator.createNewEmptyWorld()
        WorldEditUtils.paste(
            newWorld, file, Vector(0, 64, 0)
        )

        return newWorld
    }
}
