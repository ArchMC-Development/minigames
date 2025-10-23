package gg.tropic.practice.commands

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.agnostic.sync.ServerSync
import gg.scala.commons.agnostic.sync.server.ServerContainer.getServersInGroup
import gg.scala.commons.agnostic.sync.server.impl.GameServer
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.scala.lemon.redirection.impl.VelocityRedirectSystem
import gg.tropic.practice.configuration.PracticeConfigurationService
import gg.tropic.practice.lobbyGroup
import gg.tropic.practice.minigame.MinigameLobby
import gg.tropic.practice.suffixWhenDev
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ColorUtil.toWoolData
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 4/2/2023
 */
@AutoRegister
object SwitchLobbyServerCommand : ScalaCommand()
{
    class SwitchLobbyServerMenu : PaginatedMenu()
    {
        init
        {
            autoUpdate = true
        }

        override fun getAllPagesButtons(player: Player): Map<Int, Button>
        {
            val buttons = mutableMapOf<Int, Button>()
            val lobbyGroup = when (true)
            {
                MinigameLobby.isMainLobby() -> "hub"
                MinigameLobby.isMinigameLobby() -> PracticeConfigurationService.minigameType().provide().lobbyGroup
                else -> lobbyGroup()
            }

            val lobbyDisplayName = when (true)
            {
                MinigameLobby.isMainLobby() -> "Main"
                MinigameLobby.isMinigameLobby() -> PracticeConfigurationService.minigameType().provide().displayName
                else -> "Duels"
            }

            getServersInGroup(lobbyGroup)
                .sortedBy {
                    it.id
                }
                .filterIsInstance<GameServer>()
                .forEachIndexed { index, gameServer ->
                    buttons[buttons.size] = ItemBuilder.of(Material.WOOL)
                        .data(
                            toWoolData(
                                if (gameServer.getWhitelisted()!!)
                                    ChatColor.RED
                                else
                                    if (gameServer.getPlayersCount()!! >= gameServer.getMaxPlayers()!!)
                                        ChatColor.GRAY
                                    else
                                        if (gameServer.id == ServerSync.local.id)
                                            ChatColor.LIGHT_PURPLE else ChatColor.GREEN
                            ).toShort()
                        )
                        .name("${CC.GREEN}$lobbyDisplayName Lobby #${index + 1}")
                        .addToLore(
                            CC.GRAY + gameServer.getPlayersCount() + "/" + gameServer.getMaxPlayers() + " online...",
                            "",
                            (if (gameServer.getWhitelisted()!!)
                                ChatColor.RED.toString() + "Server is whitelisted!"
                            else
                                if (gameServer.getPlayersCount()!! >= gameServer.getMaxPlayers()!!)
                                    ChatColor.RED.toString() + "Server is full!"
                                else
                                    ChatColor.YELLOW.toString() + "Click to join!")
                        )
                        .toButton { _, _ ->
                            redirectToServer(
                                player,
                                gameServer
                            )
                        }
                }

            return buttons
        }

        override fun getPrePaginatedTitle(player: Player) = "Lobby Selector"

        fun redirectToServer(
            player: Player, gameServer: GameServer
        )
        {
            Button.playNeutral(player)
            if (gameServer.id == ServerSync.local.id)
            {
                player.sendMessage("${CC.RED}Already connected!")
                return
            }

            if (gameServer.getWhitelisted()!!)
            {
                player.sendMessage("${CC.RED}Server is whitelisted!")
                return
            }

            if (gameServer.getMetadataValue<Boolean>("server", "kubernetes-draining") == true)
            {
                player.sendMessage("${CC.RED}Server is scheduled to reboot!")
                return
            }

            if (gameServer.getPlayersCount()!! >= gameServer.getMaxPlayers()!!)
            {
                player.sendMessage("${CC.RED}Server is full!")
                return
            }

            player.closeInventory()
            player.sendMessage("${CC.GREEN}Switching lobbies...")

            VelocityRedirectSystem
                .redirect(player, gameServer.id)
        }
    }

    @CommandAlias("switchlobby|switchserver")
    fun onSwitchLobby(player: ScalaPlayer)
    {
        SwitchLobbyServerMenu().openMenu(player.bukkit())
    }
}
