package gg.tropic.practice.quests.model.tracker

/**
 * @author Subham
 * @since 7/8/25
 */
data class QuestTracker(
    val questID: String,
    var state: QuestTrackerState
)
