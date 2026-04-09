package gg.tropic.practice.commands

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.tropic.practice.configuration.PracticeConfigurationService
import gg.tropic.practice.menu.LeaderboardsMenu

@AutoRegister
object PrototypeCommand : ScalaCommand()
{
    @CommandAlias("prototype|prototypes")
    fun onPrototype(player: ScalaPlayer)
    {
        PracticeConfigurationService.openEditableUI(player.bukkit(), "prototype")
    }
}
