package gg.solara.practice.editor

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.ChunkGenerator
import java.util.*

/**
 * @author GrowlyX
 * @since 7/18/2024
 */
object EditorGenerator
{

    object VoidWorldGenerator : ChunkGenerator()
    {
        @Deprecated(
            "Deprecated in Java",
            ReplaceWith("ByteArray(32768)")
        )
        override fun generate(
            world: World?, random: Random?, x: Int, z: Int
        ) = ByteArray(32768)

        override fun canSpawn(world: World?, x: Int, z: Int) = true
        override fun getDefaultPopulators(world: World?) = listOf<BlockPopulator>()
        override fun getFixedSpawnLocation(world: World?, random: Random?) = Location(world, 0.0, 62.0, 0.0)
    }

    fun createNewEmptyWorld(): World
    {
        return WorldCreator
            .name("editor_temp_${UUID.randomUUID()}")
            .generator(VoidWorldGenerator)
            .createWorld()
            .apply {
                setGameRuleValue("doMobSpawning", "false")
            }
    }

}
