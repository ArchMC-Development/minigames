package gg.tropic.practice.ugc.generation

import gg.tropic.practice.ugc.generation.visits.VisitWorldRequest

/**
 * @author Subham
 * @since 7/20/25
 */
data class WorldLoadRequest(
    val visitWorldRequest: VisitWorldRequest,
    val server: String
)
