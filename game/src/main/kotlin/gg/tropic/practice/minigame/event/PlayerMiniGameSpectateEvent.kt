package gg.tropic.practice.minigame.event

import gg.scala.commons.event.StatelessEvent
import gg.tropic.practice.minigame.MiniGameLifecycle
import org.bukkit.entity.Player

data class PlayerMiniGameSpectateEvent(
    val game: MiniGameLifecycle<*>,
    val player: Player
) : StatelessEvent()
