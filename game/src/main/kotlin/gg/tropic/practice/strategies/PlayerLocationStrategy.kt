package gg.tropic.practice.strategies

import gg.tropic.practice.games.GameService
import gg.tropic.practice.ugc.toHostedWorld
import org.bukkit.World
import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 8/3/25
 */
object PlayerLocationStrategy
{
    fun findPlayerExpectedWorld(player: Player): World?
    {
        val game = GameService.byPlayerOrSpectator(player.uniqueId)
        if (game != null)
        {
            return game.arenaWorld
        }

        val hostedWorld = player.toHostedWorld()
        if (hostedWorld != null)
        {
            return hostedWorld.bukkitWorld
        }

        return null
    }
}
