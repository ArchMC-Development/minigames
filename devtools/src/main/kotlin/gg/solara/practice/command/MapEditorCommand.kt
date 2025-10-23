package gg.solara.practice.command

import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.*
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.scala.flavor.inject.Inject
import gg.solara.practice.PracticeDevTools
import gg.solara.practice.editor.Editor
import gg.solara.practice.editor.mapeditor.MapEditor
import gg.solara.practice.map.MapManageServices
import net.evilblock.cubed.util.CC

/**
 * @author GrowlyX
 * @since 7/18/2024
 */
@AutoRegister
@CommandPermission("op")
@CommandAlias("mapeditor")
object MapEditorCommand : ScalaCommand()
{
    @Inject
    lateinit var plugin: PracticeDevTools

    @Default
    fun default(player: ScalaPlayer)
    {
        if (Editor.instances[player.uniqueId] != null)
        {
            throw ConditionFailedException("Already in editor, do /editor leave!")
        }

        if (MapEditor.instances[player.uniqueId] != null)
        {
            throw ConditionFailedException("Already in map editor, do /mapeditor leave!")
        }

        player.sendMessage("${CC.GOLD}Entering the editor...")
        MapEditor(player.bukkit(), MapManageServices.loader).apply {
            terminable.bindWith(plugin)
        }.initialize()
    }

    @Subcommand("leave")
    fun leave(player: ScalaPlayer)
    {
        if (MapEditor.instances[player.uniqueId] == null)
        {
            throw ConditionFailedException("Not in editor!")
        }

        MapEditor.instances[player.uniqueId]?.terminable?.closeAndReportException()
        MapEditor.instances.remove(player.uniqueId)

        player.sendMessage("${CC.RED}Left editor.")
    }
}
