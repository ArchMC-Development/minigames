package gg.tropic.practice.minigame.event

import gg.scala.commons.event.StatelessEvent
import gg.tropic.practice.minigame.AbstractMiniGameGameImpl
import gg.tropic.practice.minigame.MiniGameLifecycle
import org.bukkit.entity.Player
import java.util.UUID

/**
 * @author GrowlyX
 * @since 8/24/2024
 */
data class PlayerMiniGameSpectateWithTokenEvent(
    val game: MiniGameLifecycle<*>,
    val player: Player
) : StatelessEvent()
