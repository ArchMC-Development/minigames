package gg.tropic.practice.commands

import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.tropic.practice.configuration.PracticeConfigurationService
import gg.tropic.practice.parkour.isPlayingParkour

/**
 * @author GrowlyX
 * @since 3/18/2025
 */
@AutoRegister
object ParkourCommand : ScalaCommand()
{
    @CommandAlias("parkour")
    fun onParkour(player: ScalaPlayer)
    {
        if (!PracticeConfigurationService.local().isParkourReady())
        {
            throw ConditionFailedException("Parkour is not enabled on this lobby!")
        }

        if (player.bukkit().isPlayingParkour())
        {
            throw ConditionFailedException("You are already playing parkour!")
        }

        player.bukkit().teleport(
            PracticeConfigurationService.local().parkourConfiguration
                .priorStart!!
                .toLocation(player.bukkit().world)
        )
    }
}
