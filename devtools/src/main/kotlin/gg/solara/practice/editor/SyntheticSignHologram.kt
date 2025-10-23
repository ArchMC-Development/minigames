package gg.solara.practice.editor

import gg.tropic.practice.map.metadata.toSanitizedCoordinate
import net.evilblock.cubed.entity.EntityHandler
import net.evilblock.cubed.entity.hologram.HologramEntity
import net.evilblock.cubed.util.CC
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.UUID

/**
 * @author Subham
 * @since 5/23/25
 */
data class SyntheticSignHologram(
    val editor: SyntheticsEditor,
    val refID: UUID,
    val playerLocation: Location,
    val type: String,
    val extraLines: MutableList<String>
) : HologramEntity(
    "synthetic",
    Location(
        playerLocation.world,
        playerLocation.x.toSanitizedCoordinate(),
        playerLocation.y.toSanitizedCoordinate(),
        playerLocation.z.toSanitizedCoordinate(),
        playerLocation.yaw,
        playerLocation.pitch
    )
)
{
    init
    {
        // will be auto removed later on
        persistent = false
    }

    override fun onLeftClick(player: Player)
    {
        editor.synthetics.removeIf { it.refID == refID }
        destroyForCurrentWatchers()
        EntityHandler.forgetEntity(this)

        player.sendMessage("${CC.RED}Removed!")
        player.playSound(player.location, Sound.NOTE_PLING, 1.0f, 1.0f)
    }

    override fun getLines() = listOf(
        "${CC.GRAY}${CC.STRIKE_THROUGH}${"-".repeat(16)}",
        "${CC.B_WHITE}[$type]",
        *extraLines.toTypedArray(),
        "${CC.GRAY}${CC.STRIKE_THROUGH}${"-".repeat(16)}",
        "${CC.RED}[punch to remove]"
    )
}
