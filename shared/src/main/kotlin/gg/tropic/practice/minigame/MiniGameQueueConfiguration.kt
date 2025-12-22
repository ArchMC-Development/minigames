package gg.tropic.practice.minigame

import gg.tropic.practice.privategames.PrivateGameSettings

/**
 * @author Subham
 * @since 6/28/25
 */
data class MiniGameQueueConfiguration(
    val requiredMapID: String? = null,
    val excludeMiniInstance: String? = null,
    var bracket: String? = null,
    /**
     * Private games create new instances and don't track stats.
     */
    val isPrivateGame: Boolean = false,
    val privateGameSettings: PrivateGameSettings? = null
)

