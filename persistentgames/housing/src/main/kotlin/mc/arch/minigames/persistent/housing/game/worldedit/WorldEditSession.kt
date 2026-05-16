package mc.arch.minigames.persistent.housing.game.worldedit

import org.bukkit.Material
import org.bukkit.util.Vector

class WorldEditSession
{
    var pos1: Vector? = null
    var pos2: Vector? = null
    var worldName: String? = null

    var clipboard: WorldEditClipboard? = null

    fun setPos1(vec: Vector, world: String)
    {
        pos1 = vec
        worldName = world
    }

    fun setPos2(vec: Vector, world: String)
    {
        pos2 = vec
        worldName = world
    }

    fun hasFullSelection() = pos1 != null && pos2 != null && worldName != null
}

class WorldEditClipboard(
    val sizeX: Int,
    val sizeY: Int,
    val sizeZ: Int,
    val originOffset: Vector,
    val blocks: Array<Array<Array<BlockSnapshot>>>
)

data class BlockSnapshot(val material: Material, val data: Byte)
