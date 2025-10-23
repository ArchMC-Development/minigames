package gg.tropic.practice.commands

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.tropic.practice.menu.quests.PlayerFacingQuestsMenu

/**
 * @author Subham
 * @since 7/8/25
 */
object QuestsCommand : ScalaCommand()
{
    @CommandAlias("quests|quest")
    fun onQuests(player: ScalaPlayer) = PlayerFacingQuestsMenu().openMenu(player)
}
