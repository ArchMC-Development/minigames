package gg.tropic.practice.minigame

/**
 * @author Subham
 * @since 6/28/25
 */
data class MiniGameQueueConfiguration(
    val requiredMapID: String? = null,
    val excludeMiniInstance: String? = null,
    var bracket: String? = null
)
