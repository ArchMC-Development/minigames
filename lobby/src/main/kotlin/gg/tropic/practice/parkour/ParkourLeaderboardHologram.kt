package gg.tropic.practice.parkour

import net.evilblock.cubed.entity.EntityHandler
import net.evilblock.cubed.entity.hologram.updating.UpdatingHologramEntity
import net.evilblock.cubed.serializers.impl.AbstractTypeSerializable
import net.evilblock.cubed.util.CC
import org.bukkit.Location

/**
 * @author GrowlyX
 * @since 3/18/2025
 */
class ParkourLeaderboardHologram(
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

        lines += "${CC.B_YELLOW}Parkour Leaderboards"
        lines += ""
        lines += ParkourService.topTenLeaderboardEntries

        return lines
    }

    override fun getTickInterval() = 2500L
}
