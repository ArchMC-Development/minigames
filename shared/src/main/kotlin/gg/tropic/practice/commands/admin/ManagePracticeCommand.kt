package gg.tropic.practice.commands.admin

import gg.scala.commons.acf.annotation.*
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.tropic.practice.commands.menu.admin.PracticeLobbyAdminMenu

@AutoRegister
object ManagePracticeCommand : ScalaCommand()
{
    @CommandAlias("managepractice")
    @CommandPermission("practice.manage")
    fun onManagePractice(player: ScalaPlayer)
    {
        PracticeLobbyAdminMenu().openMenu(player.bukkit())
    }
}
