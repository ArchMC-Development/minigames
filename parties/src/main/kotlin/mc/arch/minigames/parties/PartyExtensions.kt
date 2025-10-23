package mc.arch.minigames.parties

import gg.scala.commons.playerstatus.PlayerStatusTrackerService
import gg.scala.lemon.util.QuickAccess.username
import mc.arch.minigames.parties.service.NetworkPartyService
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player
import java.util.UUID

/**
 * @author Subham
 * @since 7/2/25
 */
fun Player.toParty() = NetworkPartyService.findParty(uniqueId)
fun UUID.toDisplayName() = PlayerStatusTrackerService.loadStatusOf(this)
    .join()
    ?.prefixedName
    ?: "${CC.GRAY}${username()}"
