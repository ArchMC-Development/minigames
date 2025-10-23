package gg.tropic.practice.minigame.event.functionality

import gg.scala.commons.event.StatelessEvent
import gg.tropic.practice.games.damage.EliminationCause
import gg.tropic.practice.games.team.TeamIdentifier
import gg.tropic.practice.minigame.MiniGameLifecycle
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

/**
 * @author Subham
 * @since 5/25/25
 */
data class MiniGamePlayerDeathEvent(
    val game: MiniGameLifecycle<*>,
    val player: Player,
    val eliminationCause: EliminationCause,
    val killer: Entity?,
    val drops: MutableList<ItemStack>
) : StatelessEvent()
