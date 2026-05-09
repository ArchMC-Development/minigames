package mc.arch.minigames.versioned.generics

import org.bukkit.World
import java.io.File

interface SchematicProvider
{
    fun pasteSchematic(world: World, file: File, x: Int, y: Int, z: Int)

    /**
     * Creates an empty void world suitable for schematic editing.
     * Each provider handles its own ChunkGenerator API differences (1.8 byte[] generate vs Paper 1.21 ChunkData).
     */
    fun createEmptyVoidWorld(name: String): World
}
