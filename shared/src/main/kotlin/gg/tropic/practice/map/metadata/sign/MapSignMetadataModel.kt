package gg.tropic.practice.map.metadata.sign

import gg.scala.commons.spatial.Position
import gg.tropic.practice.map.metadata.scanner.AbstractMapMetadataScanner
import gg.tropic.practice.map.metadata.scanner.MetadataScannerUtilities

/**
 * @author GrowlyX
 * @since 11/10/2022
 */
data class MapSignMetadataModel(
    val metaType: String,
    val location: Position,
    var id: String,
    var extraMetadata: List<String>,
)
{
    val scanner: AbstractMapMetadataScanner<*>
        get() = MetadataScannerUtilities.matches(metaType)!!

    fun flags(id: String) = extraMetadata
        .any {
            it == id
        }

    fun valueOf(id: String) = extraMetadata
        .firstOrNull {
            it.startsWith("$id=")
        }
        ?.split("=")?.first()
}
