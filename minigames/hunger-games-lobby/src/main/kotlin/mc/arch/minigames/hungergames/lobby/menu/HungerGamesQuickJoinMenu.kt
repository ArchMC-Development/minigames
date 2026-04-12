package mc.arch.minigames.hungergames.lobby.menu

import com.cryptomorin.xseries.XMaterial
import gg.tropic.practice.minigame.joinGame
import gg.tropic.practice.minigame.toConciseJoinButton
import mc.arch.minigames.hungergames.HungerGamesTypeMetadata
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player

/**
 * @author ArchMC
 */
class HungerGamesQuickJoinMenu : Menu("Join a Survival Games game")
{
    override fun size(buttons: Map<Int, Button>) = 27
    override fun getButtons(player: Player) = mapOf(
        11 to HungerGamesTypeMetadata
            .mode("solo_normal")
            .toConciseJoinItem()
            .toConciseJoinButton("solo_normal"),
        13 to HungerGamesTypeMetadata
            .mode("doubles_normal")
            .toConciseJoinItem()
            .toConciseJoinButton("doubles_normal"),
        15 to ItemBuilder
            .of(XMaterial.EMERALD)
            .name("${CC.GREEN}Quick Join")
            .addToLore(
                "${CC.GRAY}Join any game that is",
                "${CC.GRAY}about to start!",
                "",
                "${CC.YELLOW}Click to join!"
            )
            .toButton { _, _ ->
                player.closeInventory()
                player.sendMessage("${CC.GRAY}Finding a game for you to join...")

                HungerGamesTypeMetadata
                    .computeGameTypeRequiringPlayers()
                    .thenAccept { metadata ->
                        metadata.joinGame(player)
                    }
            }
    )
}
