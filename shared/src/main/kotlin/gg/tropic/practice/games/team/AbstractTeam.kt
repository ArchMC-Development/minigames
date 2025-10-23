package gg.tropic.practice.games.team

import org.bukkit.Bukkit
import java.util.UUID

/**
 * @author GrowlyX
 * @since 8/9/2022
*/
abstract class AbstractTeam(
    val teamIdentifier: TeamIdentifier,
    val players: MutableSet<UUID>
)
{
    private var backingPlayerCombos: MutableMap<UUID, Int>? = null
        get()
        {
            if (field == null)
            {
                field = mutableMapOf()
            }
            return field
        }

    private var backingHighestPlayerCombos: MutableMap<UUID, Int>? = null
        get()
        {
            if (field == null)
            {
                field = mutableMapOf()
            }
            return field
        }

    val playerCombos: MutableMap<UUID, Int>
        get() = backingPlayerCombos!!

    val highestPlayerCombos: MutableMap<UUID, Int>
        get() = backingHighestPlayerCombos!!

    var gameLifecycleArbitraryObjectiveProgress = 0

    fun toBukkitPlayers() = this.players
        .map {
            Bukkit.getPlayer(it)
        }
}
