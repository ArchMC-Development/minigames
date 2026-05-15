package gg.tropic.practice.map.utilities

import gg.tropic.practice.map.metadata.AbstractMapMetadata
import gg.scala.commons.spatial.Bounds
import gg.tropic.practice.map.metadata.sign.MapSignMetadataModel
import gg.tropic.practice.map.metadata.sign.SignLineReader
import gg.tropic.practice.map.metadata.sign.parseIntoMetadata
import org.bukkit.World
import org.bukkit.block.Sign
import org.bukkit.util.Vector

/**
 * @author GrowlyX
 * @since 9/21/2023
 */
object MapMetadataScanUtilities
{
    fun buildMetadataFor(world: World, usedSynthetically: Boolean = false): MapMetadata
    {
        val scheduledRemoval = mutableListOf<Vector>()
        val blocks = world.loadedChunks
            .flatMap {
                it.tileEntities.toList()
            }

        val modelMappings = blocks
            .filterIsInstance<Sign>()
            .mapNotNull {
                val metadata = SignLineReader.read(it)
                    .parseIntoMetadata(it.location, usedSynthetically)

                metadata
            }
            .onEach {
                scheduledRemoval += it.location.toVector()
            }
            .groupBy {
                if (it.metaType == "spawn")
                    "${it.metaType}${it.id}@${it.location.x},${it.location.y},${it.location.z}"
                else "${it.metaType}${it.id}"
            }
            .toMutableMap()

        val metadata = mutableListOf<AbstractMapMetadata>()
        for (modelMapping in modelMappings)
        {
            val model = modelMapping.value.first()
            val scanner = model.scanner

            val scannedMetadata = runCatching {
                scanner.scan(
                    if (model.metaType == "spawn")
                        model.id
                    else modelMapping.key.removePrefix(model.metaType),
                    modelMapping.value,
                    world
                )
            }.getOrNull()

            if (scannedMetadata != null)
            {
                metadata += scannedMetadata
            }
        }

        return MapMetadata(
            metadataSignLocations = scheduledRemoval,
            metadata = metadata,
            synthetic = listOf()
        )
    }

    fun buildJustInTimeMetadata(
        models: List<MapSignMetadataModel>,
        world: World
    ): List<AbstractMapMetadata>
    {
        val modelMappings = models
            .groupBy { "${it.metaType}${it.id}" }
            .toMutableMap()

        val metadata = mutableListOf<AbstractMapMetadata>()
        for (modelMapping in modelMappings)
        {
            val model = modelMapping.value.first()
            val scanner = model.scanner

            val scannedMetadata = runCatching {
                scanner.scan(
                    modelMapping.key.removePrefix(model.metaType),
                    modelMapping.value,
                    world
                )
            }.getOrNull()

            if (scannedMetadata != null)
            {
                metadata += scannedMetadata
            }
        }

        return metadata
    }
}
