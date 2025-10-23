package gg.tropic.practice.minigame.spectators

import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 7/13/25
 */
data class PlayerParticipant(
    val player: Player,
    var displayName: String,
    var description: List<String>
)
