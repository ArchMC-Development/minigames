package gg.tropic.practice.menu

import com.cryptomorin.xseries.XSound
import gg.tropic.practice.kit.Kit
import gg.tropic.practice.kit.feature.FeatureFlag
import gg.tropic.practice.leaderboards.CommonlyViewedLeaderboardType
import gg.tropic.practice.leaderboards.StatisticLeaderboard
import gg.tropic.practice.profile.PracticeProfileService
import gg.tropic.practice.services.LeaderboardManagerService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

/**
 * @author GrowlyX
 * @since 12/16/2023
 */
class LeaderboardsMenu(player: Player) : TemplateKitMenu(player)
{
    private var menuState = CommonlyViewedLeaderboardType.ELO
    override fun filterDisplayOfKit(player: Player, kit: Kit) =
        !menuState.enforceRanked || kit.features(FeatureFlag.Ranked)

    init
    {
        updateAfterClick = true
    }

    override fun itemTitleFor(player: Player, kit: Kit) = "${CC.B_PRI}${kit.displayName}"
    override fun shouldIncludeKitDescription() = false

    override fun itemDescriptionOf(player: Player, kit: Kit) = LeaderboardManagerService.formatMenuLeaderboard(
        player,
        StatisticLeaderboard(commonlyViewedLeaderboardType = menuState, kit = kit)
    )

    override fun getGlobalButtons(player: Player): Map<Int, Button>
    {
        val buttons = super.getGlobalButtons(player)
            ?.toMutableMap() ?: mutableMapOf()

        buttons[5] = ItemBuilder
            .of(Material.ENDER_PORTAL_FRAME)
            .glow()
            .name("${CC.PRI}Toggle View")
            .addToLore(
                "${CC.GRAY}Select one of the following",
                "${CC.GRAY}views to see other leaderboards!",
                "",
                "${CC.WHITE}Current view:",
            )
            .apply {
                for (type in CommonlyViewedLeaderboardType.entries)
                {
                    if (menuState == type)
                    {
                        addToLore("${CC.GREEN}► ${type.displayName}")
                    } else
                    {
                        addToLore("${CC.GRAY}${type.displayName}")
                    }
                }
            }
            .addToLore(
                "",
                "${CC.GREEN}Click to scroll through!"
            )
            .toButton { _, type ->
                menuState = if (type!!.isRightClick)
                    menuState.previous() else
                    menuState.next()

                openMenu(player)
                player.playSound(
                    player.location,
                    XSound.BLOCK_NOTE_BLOCK_HAT.parseSound()!!,
                    1.0f, 1.0f
                )
            }

        buttons[3] = ItemBuilder
            .of(Material.NETHER_STAR)
            .name("${CC.AQUA}Your Stats")
            .addToLore("${CC.GRAY}Click to view your stats!")
            .toButton { _, _ ->
                Button.playNeutral(player)
                StatisticsMenu(player, PracticeProfileService.find(player)!!).openMenu(player)
            }

        return buttons
    }

    override fun itemClicked(player: Player, kit: Kit, type: ClickType) = Unit
    override fun getPrePaginatedTitle(player: Player) = menuState.displayName + " Leaderboards"
}
