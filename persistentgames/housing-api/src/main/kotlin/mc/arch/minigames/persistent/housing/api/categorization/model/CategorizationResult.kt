package mc.arch.minigames.persistent.housing.api.categorization.model

import java.util.UUID

/**
 * Complete pipeline output attached to a [mc.arch.minigames.persistent.housing.api.model.PlayerHouse].
 * Retains the intermediate stage outputs so downstream consumers (menus, search,
 * recommendation) can reason about *why* a category was assigned.
 *
 * [inputContentHash] keys the cache: a re-categorization is only redone when the
 * house's textual surface actually changes.
 */
data class CategorizationResult(
    val houseId: UUID,
    val inputContentHash: String,
    val pipelineVersion: String,
    val modelVersion: String,
    val insights: List<HouseInsight>,
    val topics: List<HouseTopic>,
    val categories: List<HouseCategory>,
    val producedAt: Long
)
{
    val primaryCategory: HouseCategory? get() = categories
        .filter { it.scope == HouseCategory.Scope.IN_HOUSE }
        .maxByOrNull { it.confidence }
}
