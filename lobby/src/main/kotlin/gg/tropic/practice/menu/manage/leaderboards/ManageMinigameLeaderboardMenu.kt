package gg.tropic.practice.menu.manage.leaderboards

import com.cryptomorin.xseries.XMaterial
import gg.scala.commons.configurable.editPosition
import gg.scala.commons.configurable.editString
import gg.tropic.practice.configuration.PracticeConfigurationService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.menu.buttons.RemoveButton
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 6/27/25
 */
class ManageMinigameLeaderboardMenu(
    private val index: Int
) : Menu("Managing Leaderboard #${index + 1}")
{
    init
    {
        placeholder = true
    }

    override fun getButtons(player: Player) = mapOf(
        10 to editPosition(
            PracticeConfigurationService,
            title = "Spawn Location",
            material = XMaterial.EMERALD,
            getter = {
                local().leaderboards[index].position
            },
            setter = {
                local().leaderboards[index].position = it
            }
        ),
        11 to editString(
            PracticeConfigurationService,
            title = "Statistic ID",
            material = XMaterial.NAME_TAG,
            getter = {
                local().leaderboards[index].statisticID
            },
            setter = {
                local().leaderboards[index].statisticID = it
            }
        ),
        12 to editString(
            PracticeConfigurationService,
            title = "Display Name",
            material = XMaterial.OAK_SIGN,
            getter = {
                local().leaderboards[index].displayName
            },
            setter = {
                local().leaderboards[index].displayName = it
            }
        ),
        16 to RemoveButton {
            PracticeConfigurationService.editAndSave {
                local().leaderboards.removeAt(index)
            }

            player.sendMessage("${CC.RED}Removed!")
            player.closeInventory()
        }
    )

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose)
        {
            Tasks.sync {
                ViewMinigameLeaderboardsMenu().openMenu(player)
            }
        }
    }

    override fun size(buttons: Map<Int, Button>) = 27
}
