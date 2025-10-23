package gg.tropic.practice.games.damage

import org.bukkit.Material

data class EnvironmentalContext(
    val nearbyBlocks: Set<Material>,
    val wasMovingFast: Boolean,
    val wasInCombat: Boolean,
    val wasNearVoid: Boolean = false,
    val wasNearLava: Boolean = false
)
