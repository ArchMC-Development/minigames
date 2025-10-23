package gg.tropic.practice.parkour

import com.cryptomorin.xseries.XMaterial
import gg.scala.commons.configurable.editBounds
import gg.scala.commons.configurable.editPosition
import gg.scala.commons.spatial.Bounds
import gg.scala.commons.spatial.Position
import gg.tropic.practice.configuration.PracticeConfigurationService
import gg.tropic.practice.menu.manage.ManageLobbyMenu
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 3/18/2025
 */
class ManageParkourMenu : Menu("Manage Parkour Configuration")
{
    init
    {
        placeholder = true
    }

    override fun getButtons(player: Player) = mapOf(
        10 to editPosition(
            PracticeConfigurationService,
            title = "Parkour Start",
            material = XMaterial.HEAVY_WEIGHTED_PRESSURE_PLATE,
            getter = {
                local().parkourConfiguration.priorStart ?: Position(0.0, 0.0, 0.0)
            },
            setter = {
                local().parkourConfiguration.priorStart = it
            }
        ),
        11 to editBounds(
            PracticeConfigurationService,
            title = "Parkour Start Activation Bound",
            material = XMaterial.OAK_WOOD,
            getter = {
                local().parkourConfiguration.start ?: Bounds(
                    Position(0.0, 0.0, 0.0),
                    Position(0.0, 0.0, 0.0)
                )
            },
            setter = {
                local().parkourConfiguration.start = it
            }
        ),
        12 to editBounds(
            PracticeConfigurationService,
            title = "Parkour End Activation Bound",
            material = XMaterial.SPRUCE_WOOD,
            getter = {
                local().parkourConfiguration.end ?: Bounds(
                    Position(0.0, 0.0, 0.0),
                    Position(0.0, 0.0, 0.0)
                )
            },
            setter = {
                local().parkourConfiguration.end = it
            }
        ),
        13 to ItemBuilder
            .of(XMaterial.OAK_WOOD)
            .name("${CC.AQUA}Set Parkour Hologram")
            .addToLore("${CC.GRAY}[Click to set]")
            .toButton { _, _ ->
                val hologram = ParkourLeaderboardHologram(player.location)
                hologram.configure()
            }
    )

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose)
        {
            Tasks.sync {
                ManageLobbyMenu().openMenu(player)
            }
        }
    }

    override fun size(buttons: Map<Int, Button>) = 27
}
