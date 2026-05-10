package gg.tropic.practice.map.metadata.sign

import org.bukkit.block.Block
import org.bukkit.block.BlockFace

/**
 * Cross-version sign facing lookup. Modern Paper exposes facing via `BlockData`
 * (`Rotatable` standing / `Directional` wall); 1.8 uses `material.Sign#getFacing`.
 * Reflective dispatch keeps shared compiling against either Bukkit jar.
 */
object SignFacingResolver
{
    private val rotatableClass = runCatching { Class.forName("org.bukkit.block.data.Rotatable") }.getOrNull()
    private val directionalClass = runCatching { Class.forName("org.bukkit.block.data.Directional") }.getOrNull()
    private val rotatableGetRotation = rotatableClass?.runCatching { getMethod("getRotation") }?.getOrNull()
    private val directionalGetFacing = directionalClass?.runCatching { getMethod("getFacing") }?.getOrNull()

    private val legacySignClass = runCatching { Class.forName("org.bukkit.material.Sign") }.getOrNull()
    private val legacySignGetFacing = legacySignClass?.runCatching { getMethod("getFacing") }?.getOrNull()

    fun facingOf(block: Block): BlockFace?
    {
        runCatching {
            val data = block.javaClass.getMethod("getBlockData").invoke(block)
            if (rotatableClass?.isInstance(data) == true && rotatableGetRotation != null)
                return rotatableGetRotation.invoke(data) as? BlockFace
            if (directionalClass?.isInstance(data) == true && directionalGetFacing != null)
                return directionalGetFacing.invoke(data) as? BlockFace
        }

        runCatching {
            val state = block.state ?: return null
            val data = state.javaClass.getMethod("getData").invoke(state)
            if (legacySignClass?.isInstance(data) == true && legacySignGetFacing != null)
                return legacySignGetFacing.invoke(data) as? BlockFace
        }

        return null
    }
}
