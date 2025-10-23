package gg.tropic.practice.games

import gg.tropic.practice.metadata.InstanceMetadata

/**
 * @author GrowlyX
 * @since 9/25/2023
 */
data class SystemBulkMetadata(
    val indexes: Map<String, InstanceMetadata>
)
