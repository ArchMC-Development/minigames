package mc.arch.minigames.persistent.housing.api.categorization.model

/**
 * Normalized topic produced by stage 2 of the pipeline: a deduplicated,
 * canonical topic name aggregated from one or more [HouseInsight]s that
 * semantically align (via embedding similarity + pattern rules).
 */
data class HouseTopic(
    val name: String,
    val supportingInsightIndices: List<Int>,
    val aggregateConfidence: Double
)
