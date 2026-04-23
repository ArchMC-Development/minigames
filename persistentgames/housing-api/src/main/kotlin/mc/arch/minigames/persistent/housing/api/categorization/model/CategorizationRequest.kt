package mc.arch.minigames.persistent.housing.api.categorization.model

import java.util.UUID

/**
 * Pipeline input: the extracted textual surface of a house plus routing
 * metadata. Decoupled from [mc.arch.minigames.persistent.housing.api.model.PlayerHouse]
 * so the Python side can be exercised and replayed without the full model.
 */
data class CategorizationRequest(
    val houseId: UUID,
    val contentHash: String,
    val displayName: String,
    val description: List<String>,
    val tags: List<String>,
    val npcText: List<String>,
    val hologramText: List<String>,
    val submittedAt: Long = System.currentTimeMillis()
)
