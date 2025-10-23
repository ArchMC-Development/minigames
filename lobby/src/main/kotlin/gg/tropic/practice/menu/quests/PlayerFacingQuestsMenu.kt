package gg.tropic.practice.menu.quests

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.LemonConstants
import gg.tropic.practice.commands.ToggleAutoAcceptQuestsCommand
import gg.tropic.practice.configuration.PracticeConfigurationService
import gg.tropic.practice.quests.QuestsService
import gg.tropic.practice.statistics.StatisticLifetime
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 7/8/25
 */
class PlayerFacingQuestsMenu : Menu("${
    PracticeConfigurationService.minigameType()
        .provide().displayName
} Quests")
{
    init
    {
        async = true
    }

    override fun asyncLoadResources(player: Player, callback: (Boolean) -> Unit)
    {
        callback(true)
    }

    override fun size(buttons: Map<Int, Button>) = 27
    override fun getButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf(
            4 to ItemBuilder
                .of(
                    PracticeConfigurationService.minigameType()
                        .provide()
                        .item
                )
                .name(
                    "${CC.GREEN}${
                        PracticeConfigurationService.minigameType()
                            .provide().displayName
                    } Quests"
                )
                .addToLore(
                    "${CC.GRAY}Activating quests is a great",
                    "${CC.GRAY}way for you to gain experience",
                    "${CC.GRAY}and coins!",
                    "",
                    "${CC.GRAY}Players are able to activate",
                    "${CC.GRAY}four daily quests, and four",
                    "${CC.GRAY}weekly quests at any time.",
                    "",
                    "${CC.GRAY}Click one of the papers",
                    "${CC.GRAY}below to activate a quest!"
                )
                .toButton()
        )

        val daily = QuestsService.getActiveMinigameQuestsOfLifetime(
            minigameID =  PracticeConfigurationService.minigameType()
                .provide().internalId,
            lifetime = StatisticLifetime.Daily
        )

        (9..12).forEach { slot ->
            val index = slot - 9
            val quest = daily.getOrNull(index)
                ?: return@forEach run {
                    buttons[slot] = ItemBuilder
                        .of(XMaterial.RED_STAINED_GLASS_PANE)
                        .name("${CC.RED}No Daily Quest")
                        .addToLore(
                            "${CC.GRAY}Check back later!",
                            "",
                            "${CC.GRAY}The ${LemonConstants.SERVER_NAME} team regularly",
                            "${CC.GRAY}releases new quests for",
                            "${CC.GRAY}players to activate!",
                        )
                        .toButton()
                }

            buttons[slot] = quest.toInteractiveButton(player, this)
        }

        val weekly = QuestsService.getActiveMinigameQuestsOfLifetime(
            minigameID =  PracticeConfigurationService.minigameType()
                .provide().internalId,
            lifetime = StatisticLifetime.Weekly
        )

        (14..17).forEach { slot ->
            val index = slot - 14
            val quest = weekly.getOrNull(index)
                ?: return@forEach run {
                    buttons[slot] = ItemBuilder
                        .of(XMaterial.RED_STAINED_GLASS_PANE)
                        .name("${CC.RED}No Weekly Quest")
                        .addToLore(
                            "${CC.GRAY}Check back later!",
                            "",
                            "${CC.GRAY}The ${LemonConstants.SERVER_NAME} team regularly",
                            "${CC.GRAY}releases new quests for",
                            "${CC.GRAY}players to activate!",
                        )
                        .toButton()
                }

            buttons[slot] = quest.toInteractiveButton(player, this)
        }

        val autoAccepting = QuestsService.isAutoAccepting(player)
        buttons[22] = ItemBuilder
            .of(XMaterial.EMERALD)
            .name("${CC.GREEN}Auto Accepting Quests")
            .addToLore(
                "${CC.GRAY}Automatically enable quests",
                "${CC.GRAY}when you log on.",
                "${CC.GRAY}",
                "${CC.GRAY}Requires ${CC.AQUA}Majestic${CC.GRAY} rank!",
                "",
                "${CC.GRAY}Status: ",
                if (autoAccepting) "${CC.GREEN}ENABLED" else "${CC.RED}DISABLED"
            )
            .toButton { _, _ ->
                Button.playNeutral(player)
                if (!player.hasPermission("minigame.quests.autoactivate"))
                {
                    player.sendMessage("${CC.RED}You need ${CC.AQUA}Majestic${CC.RED} rank to use this feature!")
                    return@toButton
                }

                ToggleAutoAcceptQuestsCommand.onAutoAcceptToggle(player)
                openMenu(player)
            }

        return buttons
    }
}
