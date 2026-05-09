package mc.arch.minigames.versioned.modern

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.session.ClipboardHolder
import mc.arch.minigames.versioned.generics.SchematicProvider
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo
import java.io.File
import java.util.Random

object ModernSchematicProvider : SchematicProvider
{
    override fun pasteSchematic(world: World, file: File, x: Int, y: Int, z: Int)
    {
        val format = ClipboardFormats.findByFile(file)
            ?: error("Unknown schematic format for ${file.name}")

        format.getReader(file.inputStream()).use { reader ->
            val clipboard = reader.read()
            val weWorld = BukkitAdapter.adapt(world)
            WorldEdit.getInstance().newEditSession(weWorld).use { editSession ->
                val operation = ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(x, y, z))
                    .ignoreAirBlocks(false)
                    .build()
                Operations.complete(operation)
            }
        }
    }

    override fun createEmptyVoidWorld(name: String): World
    {
        return WorldCreator.name(name)
            .generator(ModernVoidGenerator)
            .createWorld()!!
            .apply { setGameRule(org.bukkit.GameRule.DO_MOB_SPAWNING, false) }
    }

    private object ModernVoidGenerator : ChunkGenerator()
    {
        override fun canSpawn(world: World, x: Int, z: Int) = true
        override fun getDefaultPopulators(world: World) = listOf<BlockPopulator>()
        override fun getFixedSpawnLocation(world: World, random: Random) =
            Location(world, 0.0, 62.0, 0.0)

        // No generateNoise/Surface/Bedrock/Caves overrides — defaults produce a void chunk.
    }
}
