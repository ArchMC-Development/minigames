package gg.tropic.practice.games.bots

import java.util.UUID

/**
 * @author GrowlyX
 * @since 8/9/2024
 */
data class BotGameMetadata(
    val botNamespace: String,
    val botProfileID: String?,
    val kitID: String,
    val mapID: String? = null,
    /**
     * This should be part of the collection in [Globals]
     */
    val botInstances: Set<UUID>
)
