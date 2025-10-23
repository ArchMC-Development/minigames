package gg.tropic.practice.map.metadata.scanner

import gg.tropic.practice.map.metadata.AbstractMapMetadata
import gg.tropic.practice.map.metadata.sign.MapSignMetadataModel
import org.bukkit.World

/**
 * @author GrowlyX
 * @since 11/10/2022
 */
abstract class AbstractMapMetadataScanner<T : AbstractMapMetadata>
{
    abstract val type: String

    open fun isAllExtra() = false

    abstract fun scan(id: String, models: List<MapSignMetadataModel>, world: World): T?
}
