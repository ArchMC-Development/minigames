package gg.tropic.practice.games.multiround

import me.lucko.helper.terminable.composite.CompositeTerminable

data class MultiRoundGame(
    var roundNumber: Int = 1,
    var switchingRounds: Boolean = false
)
{
    var terminable = CompositeTerminable.create()
}
