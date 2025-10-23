package gg.tropic.practice.ugc.generation.visits

/**
 * @author Subham
 * @since 7/20/25
 */
data class VisitWorldResponse(
    val status: VisitWorldStatus,
    val redirectToInstance: String? = null
)
