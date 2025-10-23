package gg.solara.practice.command

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.Optional
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import net.evilblock.cubed.util.CC
import org.bukkit.block.Sign

/**
 * @author Subham
 * @since 7/22/25
 */
@AutoRegister
object SignListCommand : ScalaCommand()
{
    @CommandAlias("signlist")
    fun onSignList(player: ScalaPlayer, @Optional content: String?)
    {
        player.sendMessage(
            "${CC.GREEN}Listing all signs${
                if (content != null) " with $content in it" else ""
            }:"
        )
        player.bukkit().world.loadedChunks
            .flatMap {
                it.tileEntities.toList()
            }
            .filterIsInstance<Sign>()
            .filter {
                content == null || it.lines.any { line -> line.contains(content, true) }
            }
            .forEach { sign ->
                player.sendMessage("${CC.GRAY}${CC.STRIKE_THROUGH}------------")
                player.sendMessage("${CC.B_WHITE}[${sign.location.x}, ${sign.location.y}, ${sign.location.z}]")
                sign.lines.forEach {
                    player.sendMessage("${CC.GRAY}$it")
                }
                player.sendMessage("${CC.GRAY}${CC.STRIKE_THROUGH}------------")
            }
    }
}
