package gg.tropic.practice.menu.manage.top3

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
class ManageTop3LeaderboardMenu(
    private val index: Int
) : Menu("Managing Top3 #${index + 1}")
{
    init
    {
        placeholder = true
    }

    override fun getButtons(player: Player) = mapOf(
        10 to editPosition(
            PracticeConfigurationService,
            title = "Hologram Location (4th - 10th)",
            material = XMaterial.EMERALD,
            getter = {
                local().topPlayerNPCSets[index].hologram
            },
            setter = {
                local().topPlayerNPCSets[index].hologram = it
            }
        ),
        11 to editString(
            PracticeConfigurationService,
            title = "Statistic ID",
            material = XMaterial.NAME_TAG,
            getter = {
                local().topPlayerNPCSets[index].statisticID
            },
            setter = {
                local().topPlayerNPCSets[index].statisticID = it
            }
        ),
        12 to editString(
            PracticeConfigurationService,
            title = "Display Name",
            material = XMaterial.OAK_SIGN,
            getter = {
                local().topPlayerNPCSets[index].displayName
            },
            setter = {
                local().topPlayerNPCSets[index].displayName = it
            }
        ),
        13 to editPosition(
            PracticeConfigurationService,
            title = "1st Location",
            material = XMaterial.EMERALD,
            getter = {
                local().topPlayerNPCSets[index].first
            },
            setter = {
                local().topPlayerNPCSets[index].first = it
            }
        ),
        14 to editPosition(
            PracticeConfigurationService,
            title = "2nd Location",
            material = XMaterial.EMERALD,
            getter = {
                local().topPlayerNPCSets[index].second
            },
            setter = {
                local().topPlayerNPCSets[index].second = it
            }
        ),
        15 to editPosition(
            PracticeConfigurationService,
            title = "3rd Location",
            material = XMaterial.EMERALD,
            getter = {
                local().topPlayerNPCSets[index].third
            },
            setter = {
                local().topPlayerNPCSets[index].third = it
            }
        ),
        15 to editString(
            PracticeConfigurationService,
            title = "Statistic Display Name",
            material = XMaterial.NAME_TAG,
            getter = {
                local().topPlayerNPCSets[index].statisticDisplayName
            },
            setter = {
                local().topPlayerNPCSets[index].statisticDisplayName = it
            }
        ),
        16 to RemoveButton {
            PracticeConfigurationService.editAndSave {
                local().topPlayerNPCSets.removeAt(index)
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
                ViewTop3LeaderboardsMenu().openMenu(player)
            }
        }
    }

    override fun size(buttons: Map<Int, Button>) = 27
}
