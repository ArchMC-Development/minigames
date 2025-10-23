package gg.tropic.practice.games.team

import gg.tropic.practice.games.player.SpectatingPlayerProvider
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author GrowlyX
 * @since 8/9/2022
*/
open class GameTeam(
    teamIdentifier: TeamIdentifier,
    players: MutableSet<UUID>
) : AbstractTeam(
    teamIdentifier,
    ConcurrentHashMap.newKeySet<UUID>()
        .apply { addAll(players) }
)
{
    fun nonSpectators() = this.toBukkitPlayers()
        .filterNotNull()
        .filter {
            !SpectatingPlayerProvider.spectating(it)
        }
}
