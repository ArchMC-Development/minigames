package mc.arch.minigames.persistent.housing.game.command

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import org.bukkit.Material

/**
 * Class created on 12/23/2025

 * @author Max C.
 * @project arch-minigames
 * @website https://solo.to/redis
 */
@AutoRegister
object RealmAdminCommands: ScalaCommand()
{

    @CommandAlias("testplatform")
    @CommandPermission("realms.testplatform")
    fun onTestPlatform(sender: ScalaPlayer)
    {
        val player = sender.bukkit()
        val location = player.location.clone().subtract(0.0, 1.0, 0.0)

        location.block.type = Material.COBBLESTONE
    }
}