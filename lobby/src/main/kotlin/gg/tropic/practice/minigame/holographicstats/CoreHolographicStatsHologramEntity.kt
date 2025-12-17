package gg.tropic.practice.minigame.holographicstats

import gg.scala.commons.spatial.Position
import gg.tropic.practice.minigame.MinigameCompetitiveCustomizer
import gg.tropic.practice.minigame.MinigameLobby
import net.evilblock.cubed.entity.hologram.personalized.PersonalizedHologramEntity
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 7/15/25
 */
class CoreHolographicStatsHologramEntity(
    position: Position
) : PersonalizedHologramEntity(
    position.toLocation(Bukkit.getWorlds().first())
)
{
    init
    {
        persistent = false
    }

    override fun getNewLines(player: Player) = listOf(
        "${CC.B_WHITE}PERSONAL STATS",
        "",
        *(MinigameLobby.customizer() as MinigameCompetitiveCustomizer)
            .holographicStatsProvider(player)
            .map { text ->
                if (text.isBlank())
                {
                    return@map ""
                }

                return@map "${CC.PRI}${Constants.THIN_VERTICAL_LINE} ${CC.GRAY}$text"
            }
            .toTypedArray(),
        "",
        "${CC.WHITE}Use /stats to view more!"
    )

    override fun getUpdateInterval() = 1000L
}
