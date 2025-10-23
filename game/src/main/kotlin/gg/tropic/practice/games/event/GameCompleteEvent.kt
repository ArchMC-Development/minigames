package gg.tropic.practice.games.event

import gg.tropic.practice.games.GameImpl
import gg.scala.commons.event.StatefulEvent

/**
 * @author GrowlyX
 * @since 8/5/2022
 */
class GameCompleteEvent(val game: GameImpl) : StatefulEvent()
