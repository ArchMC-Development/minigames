package gg.tropic.practice.command

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.Conditions
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.tropic.practice.strategies.MarkSpectatorStrategy

/**
 * @author Subham
 * @since 8/3/25
 */
@AutoRegister
object PlayAgainCommand : ScalaCommand()
{
    @CommandAlias("playagain")
    fun onPlayAgain(
        @Conditions("cooldown:duration=10,unit=SECONDS")
        player: ScalaPlayer
    )
    {
        MarkSpectatorStrategy.playAgain(player.bukkit())
    }
}
