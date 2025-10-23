package gg.tropic.practice.games

import gg.tropic.practice.expectation.GameExpectation
import gg.tropic.practice.games.multiround.MultiRoundGame
import gg.tropic.practice.games.team.AbstractTeam
import gg.tropic.practice.games.team.GameTeam
import gg.tropic.practice.kit.Kit
import java.util.*

/**
 * @author GrowlyX
 * @since 8/5/2022
 */
abstract class AbstractGame<T : AbstractTeam>(
    val expectationModel: GameExpectation,
    var teams: Set<T>,
    val kit: Kit,
    val expectation: UUID = expectationModel.identifier
)
{
    val identifier: UUID
        get() = this.expectation

    var report: GameReport? = null
    var startTimestamp = -1L
    val multiRoundGame = MultiRoundGame()
}
