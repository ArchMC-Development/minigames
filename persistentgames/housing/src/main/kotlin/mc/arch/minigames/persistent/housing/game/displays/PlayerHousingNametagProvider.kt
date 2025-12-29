package mc.arch.minigames.persistent.housing.game.displays

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import mc.arch.minigames.persistent.housing.game.resources.getPlayerHouseFromInstance
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.nametag.NametagInfo
import net.evilblock.cubed.nametag.NametagProvider
import org.bukkit.entity.Player

/**
 * Class created on 12/28/2025

 * @author Max C.
 * @project arch-minigames
 * @website https://solo.to/redis
 */
@Service
object PlayerHousingNametagProvider : NametagProvider("realms", Int.MAX_VALUE - 40000)
{
    @Configure
    fun configure()
    {
        NametagHandler.registerProvider(this)
    }

    override fun fetchNametag(toRefresh: Player, refreshFor: Player): NametagInfo?
    {
        val house = toRefresh.getPlayerHouseFromInstance()
            ?: return null
        val role = house.getRole(toRefresh.uniqueId)

        return createNametag(
            prefix = "${role.prefix} ",
            suffix = "",
            priority = 100
        )
    }
}