package gg.tropic.practice.command

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.scala.flavor.inject.Inject
import gg.scala.lemon.redirection.aggregate.ServerAggregateHandler
import gg.scala.lemon.redirection.impl.VelocityRedirectSystem
import gg.tropic.practice.games.GameService
import gg.tropic.practice.ugc.toHostedWorld
import net.evilblock.cubed.util.CC

/**
 * @author GrowlyX
 * @since 10/28/2023
 */
@AutoRegister
object ReturnToSpawnCommand : ScalaCommand()
{
    @Inject
    lateinit var lobbyRedirector: ServerAggregateHandler

    @CommandAlias("leave|spawn|quit")
    fun onLeave(player: ScalaPlayer)
    {
        val hostedWorld = player.bukkit().toHostedWorld()
        if (hostedWorld != null)
        {
            player.bukkit().teleport(hostedWorld.playerSpawnLocation())
            player.sendMessage("${CC.GREEN}You have been sent to the spawn.")
            return
        }

        player.sendMessage("${CC.GREEN}You are being sent to a lobby...")

        val game = GameService.byPlayerOrSpectator(player.uniqueId)
            ?: return run {
                lobbyRedirector.redirect(player.bukkit())
            }

        game.findBestAvailableLobby()
            ?.apply {
                VelocityRedirectSystem.redirect(player.bukkit(), id)
            }
            ?: lobbyRedirector.redirect(player.bukkit())
    }
}
