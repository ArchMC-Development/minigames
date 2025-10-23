package gg.tropic.practice.menu.manage.quests

import com.cryptomorin.xseries.XMaterial
import gg.scala.commons.configurable.editInt
import gg.scala.commons.configurable.editListSelector
import gg.scala.commons.configurable.editString
import gg.tropic.game.extensions.economy.EconomyDataSync
import gg.tropic.practice.configuration.PracticeConfigurationService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 6/27/25
 */
class ManageQuestRewardMenu(
    private val questId: String,
    private val rewardIndex: Int
) : Menu("Managing Reward #${rewardIndex + 1}")
{
    init
    {
        placeholder = true
    }

    override fun getButtons(player: Player) = mapOf(
        11 to editListSelector(
            PracticeConfigurationService,
            title = "Economy",
            material = XMaterial.MAP,
            getter = { local().quests[questId]?.rewards?.getOrNull(rewardIndex)?.economyID!! },
            values = {
                EconomyDataSync.cached().economies.keys.toList()
            },
            setter = {
                local().quests[questId]?.rewards?.getOrNull(rewardIndex)?.economyID = it
            },
            format = {
                EconomyDataSync.cached().economies[this]
                    ?.let { "${it.currency.color}${it.currency.name} ${it.currency.symbol}" }
                    ?: this
            }
        ),
        15 to editInt(
            PracticeConfigurationService,
            title = "Amount",
            material = XMaterial.GOLD_NUGGET,
            getter = {
                local().quests[questId]?.rewards?.getOrNull(rewardIndex)?.amount?.toInt() ?: 0
            },
            setter = {
                local().quests[questId]?.rewards?.getOrNull(rewardIndex)?.amount = it.toLong()
            }
        )
    )

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose)
        {
            Tasks.sync {
                ManageQuestRewardsMenu(questId).openMenu(player)
            }
        }
    }

    override fun size(buttons: Map<Int, Button>) = 27
}
