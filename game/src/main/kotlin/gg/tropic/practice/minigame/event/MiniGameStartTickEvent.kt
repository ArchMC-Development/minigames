package gg.tropic.practice.minigame.event

import gg.scala.commons.event.StatelessEvent
import gg.tropic.practice.minigame.AbstractMiniGameGameImpl

/**
 * @author GrowlyX
 * @since 8/24/2024
 */
data class MiniGameStartTickEvent(
    val game: AbstractMiniGameGameImpl<*>,
    val secondsLeftUntilStart: Int
) : StatelessEvent()
