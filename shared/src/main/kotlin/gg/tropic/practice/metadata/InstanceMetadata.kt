package gg.tropic.practice.metadata

import gg.tropic.practice.games.GameReference
import gg.tropic.practice.provider.MiniProviderType
import gg.tropic.practice.provider.MiniProviderVersion
import gg.tropic.practice.ugc.HostedWorldInstanceReference
import gg.tropic.practice.ugc.WorldInstanceProviderType

/**
 * @author GrowlyX
 * @since 9/25/2023
 */
data class InstanceMetadata(
    val availableMinigames: Set<String>,
    val version: MiniProviderVersion,
    val supportedTypes: Set<MiniProviderType>,
    val games: List<GameReference>,
    val supportedHostedWorldProviderTypes: Set<WorldInstanceProviderType>,
    val hostedWorldInstanceReferences: List<HostedWorldInstanceReference>
)
