package gg.tropic.practice.games

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

data class GameRemovalEvent(
    val drops: MutableList<ItemStack>,
    val shouldRespawn: Boolean = true,
    val volatileState: Boolean = false,
    val killerPlayerOverride: Player? = null,
    val alternativeDeath: Boolean = false,
    val alternativeDeathMethod: String = "",
)
