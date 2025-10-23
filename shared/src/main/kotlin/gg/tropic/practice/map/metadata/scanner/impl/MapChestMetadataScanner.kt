package gg.tropic.practice.map.metadata.scanner.impl

import gg.tropic.practice.map.metadata.impl.MapChestMetadata
import gg.tropic.practice.map.metadata.scanner.AbstractMapMetadataScanner
import gg.tropic.practice.map.metadata.sign.MapSignMetadataModel
import gg.tropic.practice.map.metadata.sign.normalize
import org.bukkit.World

/**
 * @author GrowlyX
 * @since 11/10/2022
 */
object MapChestMetadataScanner : AbstractMapMetadataScanner<MapChestMetadata>()
{
    override val type = "chest"

    override fun isAllExtra() = true
    override fun scan(
        id: String, models: List<MapSignMetadataModel>, world: World
    ): MapChestMetadata
    {
        val model = models
            .groupBy {
                val scopedID = it.extraMetadata.firstOrNull()
                if (scopedID.isNullOrBlank())
                {
                    return@groupBy "global"
                }

                return@groupBy scopedID
            }
            .mapValues { grouped ->
                grouped.value.normalize(world)
            }

        return MapChestMetadata(id, model)
    }
}
