package mc.arch.minigames.hungergames.game

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import java.util.*

/**
 * Represents a glass cage built around a spawn location
 * to contain players during the pre-game waiting phase.
 *
 * The player stands on a 2-high platform. The cage surrounds
 * the 1x2 area at the player's feet (Y) and head (Y+1) level,
 * with a glass ceiling at Y+2.
 *
 */
class GlassCage(
    val spawnLocation: Location
)
{
    var occupant: UUID? = null
        private set

    private val cageBlocks = mutableListOf<Block>()

    fun isOccupied() = occupant != null

    fun assign(player: UUID)
    {
        occupant = player
    }

    fun release()
    {
        occupant = null
    }

    /**
     * Builds the glass cage around the spawn location.
     * The spawn location is where the player's feet are.
     *
     * Layout (top-down view, player at P):
     * ```
     *  GGG
     *  GPG
     *  GGG
     * ```
     * Built at Y (feet) and Y+1 (head) levels, with a floor
     * at Y-1 and a ceiling at Y+2.
     */
    fun build()
    {
        val world = spawnLocation.world
        val x = spawnLocation.blockX
        val y = spawnLocation.blockY
        val z = spawnLocation.blockZ

        // All 8 surrounding offsets (cardinal + diagonal)
        val surroundingOffsets = listOf(
            intArrayOf(-1, -1), intArrayOf(0, -1), intArrayOf(1, -1),
            intArrayOf(-1, 0),                      intArrayOf(1, 0),
            intArrayOf(-1, 1),  intArrayOf(0, 1),  intArrayOf(1, 1)
        )

        // Walls at feet level (Y) and head level (Y+1)
        for (dy in 0..1)
        {
            for (offset in surroundingOffsets)
            {
                val block = world.getBlockAt(x + offset[0], y + dy, z + offset[1])
                block.type = Material.GLASS
                cageBlocks.add(block)
            }
        }

        // Floor at Y-1
        val floor = world.getBlockAt(x, y - 1, z)
        floor.type = Material.GLASS
        cageBlocks.add(floor)

        // Ceiling at Y+2
        val ceiling = world.getBlockAt(x, y + 2, z)
        ceiling.type = Material.GLASS
        cageBlocks.add(ceiling)
    }

    /**
     * Removes all glass blocks placed by this cage,
     * setting them back to AIR.
     */
    fun destroy()
    {
        for (block in cageBlocks)
        {
            if (block.type == Material.GLASS)
            {
                block.type = Material.AIR
            }
        }
        cageBlocks.clear()
    }
}
