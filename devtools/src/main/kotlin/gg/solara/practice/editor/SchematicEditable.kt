package gg.solara.practice.editor

import com.cryptomorin.xseries.XMaterial
import gg.tropic.practice.provider.MiniProviderVersion
import gg.tropic.practice.versioned.Versioned
import org.bukkit.World
import org.bukkit.entity.Player
import java.io.File

/**
 * @author Subham
 * @since 6/24/25
 */
class SchematicEditable(
    private val file: File,
    override val version: MiniProviderVersion
) : Editable
{
    override val icon: XMaterial
        get() = XMaterial.PAPER
    override val displayName: String
        get() = file.name.split(".").first()

    override fun prepareWorld(player: Player): World
    {
        val newWorld = EditorGenerator.createNewEmptyWorld()
        Versioned.toProvider()
            .getSchematicProvider()
            .pasteSchematic(newWorld, file, 0, 64, 0)

        return newWorld
    }
}
