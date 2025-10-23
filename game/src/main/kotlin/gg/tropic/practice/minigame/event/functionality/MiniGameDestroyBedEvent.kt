package gg.tropic.practice.minigame.event.functionality

import gg.scala.commons.event.StatelessEvent
import gg.tropic.practice.games.team.TeamIdentifier
import gg.tropic.practice.minigame.MiniGameLifecycle
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent

/**
 * @author Subham
 * @since 5/25/25
 */
data class MiniGameDestroyBedEvent(
    val game: MiniGameLifecycle<*>,
    val destroyedBy: Player,
    val teamDestroyed: TeamIdentifier,
    val event: BlockBreakEvent
) : StatelessEvent()
