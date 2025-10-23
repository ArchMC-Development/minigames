package gg.tropic.practice.menu.manage.levitationportals

import com.cryptomorin.xseries.XMaterial
import gg.scala.commons.configurable.editBounds
import gg.scala.commons.configurable.editBoolean
import gg.scala.commons.configurable.editDouble
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
class ManageLevitationPortalsMenu(
    private val index: Int
) : Menu("Managing Levitation Portal #${index + 1}")
{
    init
    {
        placeholder = true
    }

    override fun getButtons(player: Player) = mapOf(
        10 to editString(
            PracticeConfigurationService,
            title = "Portal ID",
            material = XMaterial.NAME_TAG,
            getter = {
                local().levitationPortals[index].id
            },
            setter = {
                val current = local().levitationPortals[index]
                local().levitationPortals[index] = current.copy(id = it)
            }
        ),
        11 to editBounds(
            PracticeConfigurationService,
            title = "Portal Bounds",
            material = XMaterial.OAK_WOOD,
            getter = {
                local().levitationPortals[index].bounds
            },
            setter = {
                val current = local().levitationPortals[index]
                local().levitationPortals[index] = current.copy(bounds = it)
            }
        ),
        12 to editDouble(
            PracticeConfigurationService,
            title = "Levitation Height",
            material = XMaterial.FEATHER,
            range = 0.1..10.0,
            getter = {
                local().levitationPortals[index].height
            },
            setter = {
                val current = local().levitationPortals[index]
                local().levitationPortals[index] = current.copy(height = it)
            }
        ),
        13 to editDouble(
            PracticeConfigurationService,
            title = "Horizontal Limit",
            material = XMaterial.ARROW,
            {
                local().levitationPortals[index].limit
            },
            {
                val current = local().levitationPortals[index]
                local().levitationPortals[index] = current.copy(limit = it)
            }
        ),
        14 to editBoolean(
            PracticeConfigurationService,
            title = "Restrict Below Air",
            material = XMaterial.LEVER,
            getter = {
                local().levitationPortals[index].restrictBelowAir
            },
            setter = {
                val current = local().levitationPortals[index]
                local().levitationPortals[index] = current.copy(restrictBelowAir = it)
            }
        ),
        16 to RemoveButton {
            PracticeConfigurationService.editAndSave {
                local().levitationPortals.removeAt(index)
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
                ViewLevitationPortalsMenu().openMenu(player)
            }
        }
    }

    override fun size(buttons: Map<Int, Button>) = 27
}
