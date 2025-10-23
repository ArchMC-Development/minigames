package gg.tropic.practice.menu.manage.quests

import com.cryptomorin.xseries.XMaterial
import gg.scala.commons.configurable.editInt
import gg.scala.commons.configurable.editString
import gg.tropic.practice.configuration.PracticeConfigurationService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 6/27/25
 */
class ManageQuestRequirementMenu(
    private val questId: String,
    private val requirementIndex: Int
) : Menu("Managing Requirement #${requirementIndex + 1}")
{
    init
    {
        placeholder = true
    }

    override fun getButtons(player: Player) = mapOf(
        11 to editString(
            PracticeConfigurationService,
            title = "Statistic ID",
            material = XMaterial.NAME_TAG,
            getter = {
                local().quests[questId]?.requirements?.getOrNull(requirementIndex)?.statisticID ?: "unknown"
            },
            setter = {
                local().quests[questId]?.requirements?.getOrNull(requirementIndex)?.statisticID = it
            }
        ),
        13 to editString(
            PracticeConfigurationService,
            title = "Description",
            material = XMaterial.OAK_SIGN,
            getter = {
                local().quests[questId]?.requirements?.getOrNull(requirementIndex)?.description ?: "unknown"
            },
            setter = {
                local().quests[questId]?.requirements?.getOrNull(requirementIndex)?.description = it
            }
        ),
        15 to editInt(
            PracticeConfigurationService,
            title = "Required Amount",
            material = XMaterial.GOLD_NUGGET,
            getter = {
                local().quests[questId]?.requirements?.getOrNull(requirementIndex)?.requirement?.toInt() ?: 0
            },
            setter = {
                local().quests[questId]?.requirements?.getOrNull(requirementIndex)?.requirement = it.toLong()
            }
        )
    )

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose)
        {
            Tasks.sync {
                ManageQuestRequirementsMenu(questId).openMenu(player)
            }
        }
    }

    override fun size(buttons: Map<Int, Button>) = 27
}
