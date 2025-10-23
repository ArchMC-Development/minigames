package gg.tropic.practice.commands.admin.matchlist

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandCompletion
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer

/**
 * @author GrowlyX
 * @since 12/24/2023
 */
@AutoRegister
object MatchListCommand : ScalaCommand()
{
    @CommandAlias("matchlist")
    @CommandPermission("practice.command.matchlist")
    fun onMatchList(player: ScalaPlayer) = MatchListMenu().openMenu(player)
}
