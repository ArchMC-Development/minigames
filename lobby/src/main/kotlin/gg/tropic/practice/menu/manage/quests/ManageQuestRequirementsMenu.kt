package gg.tropic.practice.menu.manage.quests

import com.cryptomorin.xseries.XMaterial
import gg.tropic.practice.configuration.PracticeConfigurationService
import gg.tropic.practice.quests.model.QuestRequirement
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

/**
 * @author Subham
 * @since 6/27/25
 */
class ManageQuestRequirementsMenu(
    private val questId: String
) : PaginatedMenu()
{
    init
    {
        placeholdBorders = true
    }

    override fun getPrePaginatedTitle(player: Player) = "Quest Requirements"
    override fun getAllPagesButtonSlots() = listOf(
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25
    )

    override fun getGlobalButtons(player: Player) =
        mutableMapOf(
            4 to ItemBuilder.Companion
                .of(XMaterial.EMERALD)
                .name("${CC.B_GREEN}Add Requirement")
                .addToLore(
                    "",
                    "${CC.YELLOW}Click to add a new requirement!"
                )
                .toButton { _, _ ->
                    PracticeConfigurationService.editAndSave {
                        local().quests[questId]?.requirements?.add(QuestRequirement())
                    }

                    Button.playNeutral(player)
                    player.closeInventory()
                    player.sendMessage("${CC.GREEN}Requirement added!")
                }
        )

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()
        val quest = PracticeConfigurationService.cached().local().quests[questId]
            ?: return emptyMap()

        quest.requirements.forEachIndexed { index, requirement ->
            buttons[index] = ItemBuilder
                .of(XMaterial.PAPER)
                .name("${CC.B_YELLOW}Requirement #${index + 1}")
                .addToLore(
                    "${CC.YELLOW}Statistic ID: ${CC.WHITE}${requirement.statisticID}",
                    "${CC.YELLOW}Required Amount: ${CC.WHITE}${requirement.requirement}",
                    "",
                    "${CC.GREEN}Left-click to edit!",
                    "${CC.RED}Right-click to delete!"
                )
                .toButton { _, clickType ->
                    when (clickType)
                    {
                        ClickType.LEFT -> ManageQuestRequirementMenu(questId, index).openMenu(player)
                        ClickType.RIGHT ->
                        {
                            PracticeConfigurationService.editAndSave {
                                local().quests[questId]?.requirements?.removeAt(index)
                            }
                            Button.playNeutral(player)
                            player.closeInventory()
                            player.sendMessage("${CC.RED}Requirement deleted!")
                        }

                        else ->
                        {

                        }
                    }
                }
        }

        return buttons
    }

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose)
        {
            Tasks.sync {
                ManageQuestMenu(questId).openMenu(player)
            }
        }
    }

    override fun size(buttons: Map<Int, Button>) = 36
    override fun getMaxItemsPerPage(player: Player) = 14
}
