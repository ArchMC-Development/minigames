package gg.tropic.practice.commands

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.tropic.practice.player.hotbar.LobbyHotbarService
import net.evilblock.cubed.util.CC

@AutoRegister
object GetLobbyItemsCommand : ScalaCommand()
{
    @CommandAlias("get-lobby-items")
    @CommandPermission("op")
    fun onGetLobbyItems(player: ScalaPlayer)
    {
        LobbyHotbarService.reset(player.bukkit())
    }
}
