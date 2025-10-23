package gg.tropic.practice.games.player

import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 7/5/25
 */
object SpectatingPlayerProvider
{
    var spectating: (Player) -> Boolean = { it.hasMetadata("spectator") }
}
