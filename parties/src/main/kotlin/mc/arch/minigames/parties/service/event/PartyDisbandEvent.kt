package mc.arch.minigames.parties.service.event

import mc.arch.minigames.parties.model.Party
import net.evilblock.cubed.event.PluginEvent

/**
 * @author Subham
 * @since 12/22/25
 */
class PartyDisbandEvent(
    val party: Party
) : PluginEvent(true)
