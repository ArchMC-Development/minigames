package gg.tropic.practice.minigame.rejoin

import gg.tropic.practice.games.team.TeamIdentifier

/**
 * @author Subham
 * @since 7/1/25
 */
data class TrackedPlayerRejoin(
    val previousTeam: TeamIdentifier,
    val joinTime: Long
)
