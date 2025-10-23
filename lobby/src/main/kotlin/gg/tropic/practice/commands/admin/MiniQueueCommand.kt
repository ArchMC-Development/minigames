package gg.tropic.practice.commands.admin

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandCompletion
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Optional
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.tropic.practice.kit.Kit
import gg.tropic.practice.minigame.MiniGameQueueConfiguration
import gg.tropic.practice.queue.QueueService
import gg.tropic.practice.queue.QueueType

/**
 * @author GrowlyX
 * @since 8/24/2024
 */
@AutoRegister
object MiniQueueCommand : ScalaCommand()
{
    @CommandCompletion("@kits")
    @CommandAlias("minigamequeue")
    @CommandPermission("op")
    fun joinQueue(player: ScalaPlayer, kit: Kit, teamSize: Int, @Optional queueType: QueueType?)
    {
        player.sendMessage("Joining queue:")
        QueueService.joinQueue(kit, queueType ?: QueueType.Casual, teamSize, player.bukkit())
    }

    @CommandCompletion("@kits")
    @CommandAlias("unittest-swrankedtest")
    @CommandPermission("op")
    fun swRankedTest(player: ScalaPlayer, kit: Kit, @Optional bracket: String?)
    {
        player.sendMessage("Joining queue:")
        QueueService.joinQueue(kit, QueueType.Ranked, 1, player.bukkit(), miniGameQueueConfiguration = MiniGameQueueConfiguration(
            bracket = bracket ?: ""
        ))
    }
}
