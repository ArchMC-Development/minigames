package mc.arch.minigames.versioned.legacy

import com.grinderwolf.swm.api.world.SlimeWorld
import mc.arch.minigames.versioned.generics.SlimeWorldGeneric
import net.kyori.adventure.nbt.LongBinaryTag
import kotlin.collections.set
import kotlin.jvm.optionals.getOrNull

/**
 * @author Subham
 * @since 8/5/25
 */
class LegacySlimeWorld(override val worldInstance: SlimeWorld) : SlimeWorldGeneric<SlimeWorld>
{
    override fun clone(newName: String) = LegacySlimeWorld(
        worldInstance = worldInstance.clone(newName)
    )

    override fun getLastSave() = worldInstance.extraData
        .getAsLongTag("lastPersistentSave")
        ?.getOrNull()?.value

    override fun updateLastSave()
    {
        worldInstance.extraData.getAsLongTag("lastPersistentSave")
            .getOrNull()?.value = System.currentTimeMillis()
    }
}
