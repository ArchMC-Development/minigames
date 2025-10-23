package gg.tropic.practice.map.synthetics

import gg.tropic.practice.map.metadata.sign.MapSignMetadataModel
import net.evilblock.cubed.entity.hologram.HologramEntity
import java.util.UUID

/**
 * @author Subham
 * @since 5/23/25
 */
data class PreparedSyntheticSignModel(
    val hologramEntity: HologramEntity,
    val signMetadataModel: MapSignMetadataModel,
    val refID: UUID = UUID.randomUUID(),
)
