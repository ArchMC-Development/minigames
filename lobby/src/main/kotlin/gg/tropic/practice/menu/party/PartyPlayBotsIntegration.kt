package gg.tropic.practice.menu.party

import org.bukkit.entity.Player
import java.util.UUID

/**
 * @author GrowlyX
 * @since 8/10/2024
 */
object PartyPlayBotsIntegration
{
    var createBotsWith: (Player, Set<UUID>) -> Unit = { _, _ -> }
}
