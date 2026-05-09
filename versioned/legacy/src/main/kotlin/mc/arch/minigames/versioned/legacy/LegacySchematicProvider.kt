package mc.arch.minigames.versioned.legacy

import com.sk89q.worldedit.CuboidClipboard
import com.sk89q.worldedit.Vector
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldedit.schematic.SchematicFormat
import mc.arch.minigames.versioned.generics.SchematicProvider
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.ChunkGenerator
import java.io.File
import java.util.Random

object LegacySchematicProvider : SchematicProvider
{
    override fun pasteSchematic(world: World, file: File, x: Int, y: Int, z: Int)
    {
        val esFactory = WorldEdit.getInstance().editSessionFactory
        val editSession = esFactory.getEditSession(BukkitWorld(world), Integer.MAX_VALUE)

        val clipboard: CuboidClipboard = SchematicFormat.MCEDIT.load(file)
        clipboard.offset = Vector(0, 0, 0)
        clipboard.paste(editSession, Vector(x, y, z), true)
    }

    override fun createEmptyVoidWorld(name: String): World
    {
        return WorldCreator.name(name)
            .generator(LegacyVoidGenerator)
            .createWorld()!!
            .apply { setGameRuleValue("doMobSpawning", "false") }
    }

    private object LegacyVoidGenerator : ChunkGenerator()
    {
        @Suppress("DEPRECATION")
        override fun generate(world: World?, random: Random?, x: Int, z: Int): ByteArray =
            ByteArray(32768)

        override fun canSpawn(world: World?, x: Int, z: Int) = true
        override fun getDefaultPopulators(world: World?) = listOf<BlockPopulator>()
        override fun getFixedSpawnLocation(world: World?, random: Random?) =
            Location(world, 0.0, 62.0, 0.0)
    }
}
