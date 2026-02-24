package gg.tropic.practice.integration

import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.username
import gg.tropic.game.extensions.gems.rankgifting.RankGiftingPerkService
import net.evilblock.cubed.entity.EntityHandler
import net.evilblock.cubed.entity.hologram.updating.UpdatingHologramEntity
import net.evilblock.cubed.serializers.impl.AbstractTypeSerializable
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import org.bukkit.Location

/**
 * Class created on 2/21/2026

 * @author Max C.
 * @project arch-minigames
 * @website https://solo.to/redis
 */
class RankGiftLeaderboardHologram(
    location: Location
) : AbstractTypeSerializable, UpdatingHologramEntity("", location)
{
    fun configure()
    {
        initializeData()
        EntityHandler.trackEntity(this)
    }

    override fun getNewLines(): List<String>
    {
        val lines = mutableListOf<String>()

        lines += "${CC.B_YELLOW}Rank Gifting Leaderboard"
        lines += ""
        lines += RankGiftingPerkService.getLeaderboardPositions().thenApply { entries ->
            entries.map {
                "${
                    QuickAccess.computeColoredName(it.first, it.first.username()).join()
                } ${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.GREEN}${it.second} Ranks"
            }
        }.join()

        return lines
    }

    override fun getTickInterval() = 2500L
}
