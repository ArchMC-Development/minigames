package gg.tropic.practice.menu.manage.quests

import com.cryptomorin.xseries.XMaterial
import gg.scala.commons.configurable.editBoolean
import gg.scala.commons.configurable.editEnum
import gg.scala.commons.configurable.editString
import gg.tropic.practice.configuration.PracticeConfigurationService
import gg.tropic.practice.statistics.StatisticLifetime
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.menu.buttons.RemoveButton
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 6/27/25
 */
class ManageQuestMenu(
    private val questId: String
) : Menu("Managing Quest")
{
    init
    {
        placeholder = true
    }

    override fun getButtons(player: Player): Map<Int, Button>
    {
        val quest = PracticeConfigurationService.cached().local().quests[questId]
            ?: return emptyMap()

        return mapOf(
            10 to editString(
                PracticeConfigurationService,
                title = "Quest Name",
                material = XMaterial.NAME_TAG,
                getter = {
                    local().quests[questId]?.name ?: "Unknown"
                },
                setter = {
                    local().quests[questId]?.name = it
                }
            ),
            11 to editString(
                PracticeConfigurationService,
                title = "Description",
                material = XMaterial.PAPER,
                getter = {
                    local().quests[questId]?.description ?: "No description"
                },
                setter = {
                    local().quests[questId]?.description = it
                }
            ),
            12 to editEnum<StatisticLifetime, _>(
                PracticeConfigurationService,
                title = "Lifetime",
                material = XMaterial.CLOCK,
                getter = {
                    local().quests[questId]?.lifetime ?: StatisticLifetime.Daily
                },
                setter = {
                    local().quests[questId]?.lifetime = it as StatisticLifetime
                }
            ),
            13 to editBoolean(
                PracticeConfigurationService,
                title = "Active",
                material = XMaterial.DIAMOND,
                getter = {
                    local().quests[questId]?.active ?: false
                },
                setter = {
                    local().quests[questId]?.active = it
                }
            ),
            14 to ItemBuilder
                .of(XMaterial.EMERALD)
                .name("${CC.B_YELLOW}Manage Requirements")
                .addToLore(
                    "",
                    "${CC.YELLOW}Current requirements: ${CC.WHITE}${quest.requirements.size}",
                    *quest.requirements.map { requirement ->
                        "${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.WHITE}${requirement.statisticID}: ${CC.YELLOW}${requirement.requirement}"
                    }.toTypedArray(),
                    "",
                    "${CC.GREEN}Click to manage!"
                )
                .toButton { _, _ ->
                    ManageQuestRequirementsMenu(questId).openMenu(player)
                },
            15 to ItemBuilder
                .of(XMaterial.GOLD_INGOT)
                .name("${CC.B_YELLOW}Manage Rewards")
                .addToLore(
                    "",
                    "${CC.YELLOW}Current rewards: ${CC.WHITE}${quest.rewards.size}",
                    *quest.rewards.map { reward ->
                        reward.toFancy()
                    }.toTypedArray(),
                    "",
                    "${CC.GREEN}Click to manage!"
                )
                .toButton { _, _ ->
                    ManageQuestRewardsMenu(questId).openMenu(player)
                },
            16 to RemoveButton {
                PracticeConfigurationService.editAndSave {
                    local().quests.remove(questId)
                }

                Button.playNeutral(player)
                player.closeInventory()
                player.sendMessage("${CC.RED}Quest deleted!")
            }
        )
    }

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose)
        {
            Tasks.sync {
                ViewQuestsMenu().openMenu(player)
            }
        }
    }

    override fun size(buttons: Map<Int, Button>) = 27
}
