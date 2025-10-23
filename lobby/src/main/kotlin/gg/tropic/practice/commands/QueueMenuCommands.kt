package gg.tropic.practice.commands

import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.tropic.practice.menu.JoinQueueMenu
import gg.tropic.practice.player.hotbar.LobbyHotbarService
import gg.tropic.practice.queue.QueueType
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

@AutoRegister
object QueueMenuCommands : ScalaCommand()
{
    @CommandAlias(
        "casual|unranked"
    )
    fun onCasualCommand(player: Player)
    {
        if (player.hasMetadata("frozen"))
        {
            throw ConditionFailedException(
                "You cannot join queues while frozen!"
            )
        }

        JoinQueueMenu(player, QueueType.Casual, 1).openMenu(player)
    }

    @CommandAlias(
        "robot|robots|cpu|bots|bot|computer"
    )
    fun onRobotCommand(player: Player)
    {
        if (player.hasMetadata("frozen"))
        {
            throw ConditionFailedException(
                "${CC.RED}You cannot join queues while frozen!"
            )
        }

        val menu = LobbyHotbarService.buildBotSelectorMenu(player)
            ?: throw ConditionFailedException(
                "${CC.RED}This feature is not available on this server!"
            )

        menu.openMenu(player)
    }

    @CommandAlias(
        "ranked|comp|competitive"
    )
    fun onRankedCommand(player: Player)
    {
        LobbyHotbarService.openRankedQueueMenu(player)
    }
}
