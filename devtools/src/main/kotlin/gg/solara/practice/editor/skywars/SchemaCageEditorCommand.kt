package gg.solara.practice.editor.skywars

import gg.scala.commons.acf.CommandHelp
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandCompletion
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Default
import gg.scala.commons.acf.annotation.HelpCommand
import gg.scala.commons.acf.annotation.Subcommand
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.tropic.game.extensions.cosmetics.cages.SchemaCage
import gg.tropic.game.extensions.cosmetics.cages.SchemaCageDataSync
import net.evilblock.cubed.util.CC
import org.bukkit.Material
import org.bukkit.util.Vector

/**
 * @author GrowlyX
 * @since 8/25/2024
 */
@AutoRegister
@CommandAlias("schemacage")
@CommandPermission("op")
object SchemaCageEditorCommand : ScalaCommand()
{
    @Default
    @HelpCommand
    fun help(help: CommandHelp) = help.showHelp()

    @Subcommand("edit")
    @CommandCompletion("@schemacages")
    fun edit(player: ScalaPlayer, schemaCage: SchemaCage)
    {
        if (SchemaCageEditorSystem.instances[player.uniqueId] != null)
        {
            throw ConditionFailedException("Already editing!")
        }

        val system = SchemaCageEditorSystem(
            player.bukkit(),
            SchemaCageEditorInstance(schemaCage.id, schemaCage)
        )
        system.initialize()

        SchemaCageEditorSystem.instances[player.uniqueId] = system
    }

    @Subcommand("delete")
    @CommandCompletion("@schemacages")
    fun delete(player: ScalaPlayer, schemaCage: SchemaCage)
    {
        val cached = SchemaCageDataSync.cached()
        cached.schemas.remove(schemaCage.id)
        SchemaCageDataSync.sync(cached)
        player.sendMessage("${CC.GREEN}Deleted!")
    }

    @Subcommand("create")
    fun create(player: ScalaPlayer, id: String)
    {
        if (SchemaCageDataSync.cached().schemas[id] != null)
        {
            throw ConditionFailedException("Already exists!")
        }

        val cage = SchemaCage(id).apply {
            addBlockModifier(Vector(0, 0, 0), Material.GLASS)
        }

        val cached = SchemaCageDataSync.cached()
        cached.schemas[cage.id] = cage
        SchemaCageDataSync.sync(cached)

        player.sendMessage("${CC.GREEN}Created! Entering editor...")
        edit(player, cage)
    }
}
