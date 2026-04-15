package mc.arch.minigames.hungergames.lobby.menu

import com.cryptomorin.xseries.XMaterial
import gg.tropic.practice.menu.StatisticsMenu
import gg.tropic.practice.profile.PracticeProfileService
import mc.arch.minigames.hungergames.kits.menu.HungerGamesSelectKitMenu
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player

/**
 * @author ArchMC
 */
class HungerGamesMainMenu : Menu("Survival Games Menu")
{
    override fun size(buttons: Map<Int, Button>) = 27
    override fun getButtons(player: Player) = mapOf(
        11 to ItemBuilder
            .of(XMaterial.IRON_SWORD)
            .name("${CC.RED}Welcome to Survival Games!")
            .addToLore(
                "${CC.GRAY}Scavenge for loot in chests",
                "${CC.GRAY}scattered across the map!",
                "",
                "${CC.GRAY}Hunt down other players",
                "${CC.GRAY}and survive as long as",
                "${CC.GRAY}you can.",
                "",
                "${CC.GRAY}Be the last player standing",
                "${CC.GRAY}to claim victory!",
                "",
                "${CC.GREEN}Click to start playing!"
            )
            .toButton { _, _ ->
                HungerGamesQuickJoinMenu().openMenu(player)
            },
        13 to ItemBuilder
            .of(XMaterial.NETHER_STAR)
            .name("${CC.AQUA}Your Statistics")
            .addToLore(
                "${CC.GRAY}See how competitive you",
                "${CC.GRAY}are compared to other players!",
                "",
                "${CC.GREEN}Click to view!"
            )
            .toButton { _, _ ->
                StatisticsMenu(
                    player,
                    PracticeProfileService.find(player)!!
                ).openMenu(player)
            },
        15 to ItemBuilder
            .of(XMaterial.CHEST)
            .name("${CC.GOLD}Kits")
            .addToLore(
                "${CC.GRAY}View and select kits",
                "${CC.GRAY}for Survival Games.",
                "",
                "${CC.GREEN}Click to view!"
            )
            .toButton { _, _ ->
                HungerGamesSelectKitMenu().openMenu(player)
            },
    )
}
