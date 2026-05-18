package mc.arch.minigames.parties

import gg.scala.basics.plugin.disguise.DisguiseService
import gg.scala.commons.playerstatus.PlayerStatusTrackerService
import gg.scala.lemon.handler.RankHandler
import gg.scala.lemon.util.QuickAccess.username
import mc.arch.minigames.parties.service.NetworkPartyService
import net.evilblock.cubed.util.CC
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.util.UUID

/**
 * @author Subham
 * @since 7/2/25
 */
fun Player.toParty() = NetworkPartyService.findParty(uniqueId)
fun UUID.toDisplayName(): String
{
    val disguise = DisguiseService.getApplicantProfileMapping(this).join()
    if (disguise != null)
    {
        val rank = RankHandler.getDefaultRank()
        val prefix = if (ChatColor.stripColor(rank.prefix).isEmpty()) "" else "${rank.prefix} "
        return "$prefix${rank.color}${disguise.name}"
    }

    return PlayerStatusTrackerService.loadStatusOf(this)
        .join()
        ?.prefixedName
        ?: "${CC.GRAY}${username()}"
}
