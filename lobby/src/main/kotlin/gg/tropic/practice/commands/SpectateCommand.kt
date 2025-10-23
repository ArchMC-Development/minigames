package gg.tropic.practice.commands

import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandCompletion
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import gg.tropic.practice.games.spectate.PlayerSpectateRequest
import gg.tropic.practice.games.spectate.SpectateRequest
import gg.tropic.practice.minigame.MinigameLobby
import gg.tropic.practice.player.LobbyPlayerService
import gg.tropic.practice.player.PlayerState
import gg.tropic.practice.queue.QueueService
import net.evilblock.cubed.util.CC

/**
 * @author Elb1to
 * @since 10/19/2023
 */
@AutoRegister
object SpectateCommand : ScalaCommand()
{
    @CommandAlias("spectate|spec")
    @CommandCompletion("@mip-players")
    fun onSpectate(
        player: ScalaPlayer,
        target: AsyncLemonPlayer
    ) = target.validatePlayers(
        player.bukkit(), false
    ) {
        val lobbyProfile = LobbyPlayerService
            .find(player.bukkit())
            ?: return@validatePlayers

        if (MinigameLobby.isMinigameLobby() || MinigameLobby.isMainLobby())
        {
            if (!player.bukkit().hasPermission("minigame.staff"))
            {
                throw ConditionFailedException("This command is unavailable on this server.")
            }
        }

        if (lobbyProfile.state != PlayerState.Idle)
        {
            throw ConditionFailedException("You cannot spectate a match right now!")
        }

        it.identifier.offlineProfile
        player.bukkit().sendMessage(
            "${CC.GRAY}Loading ${it.name}'s game..."
        )

        QueueService.spectate(
            PlayerSpectateRequest(
                player.uniqueId,
                it.uniqueId,
                player.bukkit().hasPermission(
                    "practice.spectate.bypass-allowance-settings"
                )
            )
        )
    }
}
