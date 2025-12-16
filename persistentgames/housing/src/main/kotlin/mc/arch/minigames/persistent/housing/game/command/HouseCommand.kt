package mc.arch.minigames.persistent.housing.game.command

import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import mc.arch.minigames.persistent.housing.game.menu.house.MainHouseMenu
import mc.arch.minigames.persistent.housing.game.resources.getPlayerHouseFromInstance

@AutoRegister
object HouseCommand : ScalaCommand()
{
    @CommandAlias("house|home")
    fun onHouse(sender: ScalaPlayer) = sender.bukkit().getPlayerHouseFromInstance().thenAccept { house ->
        if (house == null)
        {
            throw ConditionFailedException(
                "You are not currently visiting a house!"
            )
        }

        MainHouseMenu(house, house.playerIsOrAboveAdministrator(sender.uniqueId))
            .openMenu(sender.bukkit())
    }
}