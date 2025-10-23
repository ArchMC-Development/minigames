package gg.tropic.practice.editor.commands

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Default
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.tropic.practice.editor.menu.EditableUIViewAllMenu

/**
 * @author Subham
 * @since 7/5/25
 */
@AutoRegister
@CommandAlias("manageeditableuis")
@CommandPermission("practice.command.manageeditableuis")
object ManageEditableUICommand : ScalaCommand()
{
    @Default
    fun onManage(player: ScalaPlayer) = EditableUIViewAllMenu().openMenu(player)
}
