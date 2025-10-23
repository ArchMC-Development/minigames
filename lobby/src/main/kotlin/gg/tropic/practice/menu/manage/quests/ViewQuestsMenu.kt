package gg.tropic.practice.menu.manage.quests

import com.cryptomorin.xseries.XMaterial
import gg.scala.commons.util.prompts.collectResponses
import gg.tropic.practice.configuration.PracticeConfigurationService
import gg.tropic.practice.menu.manage.ManageLobbyMenu
import gg.tropic.practice.quests.model.Quest
import me.lucko.helper.Schedulers
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 4/4/25
 */
class ViewQuestsMenu : PaginatedMenu()
{
    init
    {
        placeholdBorders = true
    }

    override fun getPrePaginatedTitle(player: Player) = "Managing Quests"
    override fun getAllPagesButtonSlots() = listOf(
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25
    )

    override fun getGlobalButtons(player: Player) =
        mutableMapOf(
            4 to ItemBuilder.Companion
                .of(XMaterial.WRITABLE_BOOK)
                .name("${CC.B_YELLOW}Create a Quest")
                .addToLore(
                    "",
                    "${CC.YELLOW}Click to create!"
                )
                .toButton { _, _ ->
                    Button.playNeutral(player)
                    player.closeInventory()
                    player.collectResponses {
                        with("id", "ID (all lowercase, no spaces)")
                        with("name", "Display Name (what players see)")
                    }.subscribe({ map ->
                        PracticeConfigurationService.editAndSave {
                            local().quests[map["id"]!!] = Quest(
                                id = map["id"]!!,
                                name = map["name"]!!
                            )
                        }

                        Schedulers
                            .async()
                            .runLater({
                                ViewQuestsMenu().openMenu(player)
                            }, 2L)
                    }, {
                        ViewQuestsMenu().openMenu(player)
                    })
                }
        )

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        PracticeConfigurationService.cached().local().quests.values
            .forEachIndexed { index, quest ->
                buttons[index] = ItemBuilder
                    .of(XMaterial.BOOK)
                    .name("${CC.B_YELLOW}${quest.name}")
                    .addToLore(
                        "${CC.YELLOW}Description: ${CC.WHITE}${quest.description}",
                        "${CC.YELLOW}Lifetime: ${CC.WHITE}${quest.lifetime}",
                        "",
                        "${CC.YELLOW}Requirements:",
                        *quest.requirements.map { requirement ->
                            "${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.WHITE}${requirement.statisticID}: ${CC.YELLOW}${requirement.requirement}"
                        }.toTypedArray(),
                        "",
                        "${CC.YELLOW}Rewards:",
                        *quest.rewards.map { reward ->
                            reward.toFancy()
                        }.toTypedArray(),
                        "",
                        "${CC.GREEN}Click to configure!"
                    )
                    .toButton { _, _ ->
                        ManageQuestMenu(quest.id).openMenu(player)
                    }
            }

        return buttons
    }

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose)
        {
            Tasks.sync {
                ManageLobbyMenu().openMenu(player)
            }
        }
    }

    override fun size(buttons: Map<Int, Button>) = 36
    override fun getMaxItemsPerPage(player: Player) = 14
}
