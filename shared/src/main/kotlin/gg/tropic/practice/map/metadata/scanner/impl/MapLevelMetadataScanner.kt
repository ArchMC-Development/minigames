package gg.tropic.practice.map.metadata.scanner.impl

import gg.tropic.practice.map.metadata.impl.MapLevelMetadata
import gg.tropic.practice.map.metadata.scanner.AbstractMapMetadataScanner
import gg.tropic.practice.map.metadata.sign.MapSignMetadataModel
import org.bukkit.World

/**
 * @author GrowlyX
 * @since 11/10/2022
 */
object MapLevelMetadataScanner : AbstractMapMetadataScanner<MapLevelMetadata>()
{
    override val type = "level"

    override fun scan(
        id: String,
        models: List<MapSignMetadataModel>,
        world: World
    ): MapLevelMetadata
    {
        val model = models.first()

        return MapLevelMetadata(
            model.location.toLocation(world).blockY,
            model.valueOf("below")?.toInt() ?: 2,
            model.flags("allowBuildOnEx")
        )
    }
}
