package gg.tropic.practice.menu.manage.scoreboard

import com.cryptomorin.xseries.XMaterial
import gg.scala.commons.configurable.editBoolean
import gg.scala.commons.configurable.editBounds
import gg.scala.commons.configurable.editEnum
import gg.scala.commons.configurable.editInt
import gg.scala.commons.configurable.editPosition
import gg.scala.commons.configurable.editString
import gg.tropic.practice.configuration.PracticeConfigurationService
import gg.tropic.practice.configuration.minigame.MotionPreset
import gg.tropic.practice.menu.manage.bezierteleporters.ViewBezierTeleportersMenu
import gg.tropic.practice.scoreboard.configuration.LobbyScoreboardConfig
import gg.tropic.practice.scoreboard.configuration.LobbyScoreboardConfigurationService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.menu.buttons.RemoveButton
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.entity.Player

/**
 * Class created on 10/12/2025

 * @author Max C.
 * @project arch-minigames
 * @website https://solo.to/redis
 */
class ManageScoreboardMenu(): Menu("Managing Scoreboard")
{
    init
    {
        placeholder = true
    }

    override fun getButtons(player: Player) = mapOf(
        10 to editBoolean(
            LobbyScoreboardConfigurationService,
            title = "Set Animation Status",
            material = XMaterial.COMPARATOR,
            getter = {
                LobbyScoreboardConfigurationService.cached().titleAnimated
            },
            setter = {
                LobbyScoreboardConfigurationService.cached().titleAnimated = it
            }
        ),
        11 to editString(
            LobbyScoreboardConfigurationService,
            title = "Fallback Title",
            material = XMaterial.ARROW,
            getter = {
                LobbyScoreboardConfigurationService.cached().title
            },
            setter = {
                LobbyScoreboardConfigurationService.cached().title = it
            }
        ),
        12 to editString(
            LobbyScoreboardConfigurationService,
            title = "Primary Color",
            material = XMaterial.BONE,
            getter = {
                "${LobbyScoreboardConfigurationService.cached().primaryColor}This"
            },
            setter = {
                LobbyScoreboardConfigurationService.cached().primaryColor = it
            }
        ),
        13 to editString(
            LobbyScoreboardConfigurationService,
            title = "Secondary Color",
            material = XMaterial.BONE_MEAL,
            getter = {
                "${LobbyScoreboardConfigurationService.cached().secondaryColor}This"
            },
            setter = {
                LobbyScoreboardConfigurationService.cached().secondaryColor = it
            }
        ),
        14 to ItemBuilder.of(XMaterial.BARRIER)
            .name("${CC.YELLOW}Reset Configuration")
            .addToLore(
                "&7Click to view"
            ).toButton { _, _ ->
                LobbyScoreboardConfigurationService.sync(LobbyScoreboardConfig())
            }
    )

    override fun size(buttons: Map<Int, Button>) = 27
}