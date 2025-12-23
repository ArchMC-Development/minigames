package mc.arch.minigames.parties.service.event

import gg.scala.commons.event.StatelessEvent
import mc.arch.minigames.parties.model.Party
import net.evilblock.cubed.event.PluginEvent
import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 12/22/25
 */
class PartyCreateEvent(
    val party: Party,
    val player: Player
) : PluginEvent(true)
