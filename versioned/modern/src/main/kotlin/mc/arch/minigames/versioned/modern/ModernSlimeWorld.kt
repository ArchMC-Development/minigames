package mc.arch.minigames.versioned.modern

import com.infernalsuite.asp.api.world.SlimeWorld
import mc.arch.minigames.versioned.generics.SlimeWorldGeneric
import net.kyori.adventure.nbt.LongBinaryTag
import kotlin.collections.set

/**
 * @author Subham
 * @since 8/5/25
 */
class ModernSlimeWorld(
    override val worldInstance: SlimeWorld
) : SlimeWorldGeneric<SlimeWorld>
{
    override fun clone(newName: String) = ModernSlimeWorld(
        worldInstance = worldInstance.clone(newName)
    )

    override fun getLastSave() = worldInstance.extraData["lastPersistentSave"]
        ?.let { binaryTag ->
            (binaryTag as LongBinaryTag).value()
        }

    override fun updateLastSave()
    {
        worldInstance.extraData["lastPersistentSave"] =
            LongBinaryTag.longBinaryTag(System.currentTimeMillis())
    }
}
