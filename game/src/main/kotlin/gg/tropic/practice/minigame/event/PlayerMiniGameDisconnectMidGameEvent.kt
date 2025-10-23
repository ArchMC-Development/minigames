package gg.tropic.practice.minigame.event

import gg.scala.commons.event.StatelessEvent
import gg.tropic.practice.minigame.AbstractMiniGameGameImpl
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 8/24/2024
 */
data class PlayerMiniGameDisconnectMidGameEvent(
    val game: AbstractMiniGameGameImpl<*>,
    val player: Player,
    var eligibleForRejoin: Boolean = true
) : StatelessEvent()
