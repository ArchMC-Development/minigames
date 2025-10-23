package mc.arch.minigames.parties.command.list

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer

/**
 * @author GrowlyX
 * @since 12/24/2023
 */
@AutoRegister
object ManagePartiesCommand : ScalaCommand()
{
    @CommandAlias("listallparties")
    @CommandPermission("parties.command.listallparties")
    fun onPartiesList(player: ScalaPlayer) = ManagePartiesMenu().openMenu(player)
}
