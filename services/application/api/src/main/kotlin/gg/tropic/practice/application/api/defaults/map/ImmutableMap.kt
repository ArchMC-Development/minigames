package gg.tropic.practice.application.api.defaults.map

import gg.tropic.practice.provider.MiniProviderVersion

/**
 * @author GrowlyX
 * @since 9/24/2023
 */
data class ImmutableMap(
    val name: String,
    val displayName: String,
    val associatedSlimeTemplate: String,
    val associatedKitGroups: Set<String>,
    val locked: Boolean,
    val version: MiniProviderVersion = MiniProviderVersion.LEGACY
)
