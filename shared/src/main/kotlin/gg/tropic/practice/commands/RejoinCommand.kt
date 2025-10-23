package gg.tropic.practice.commands

import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.Optional
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.scala.lemon.redirection.impl.VelocityRedirectSystem
import gg.tropic.practice.minigame.rejoin.destroyRejoinToken
import gg.tropic.practice.minigame.rejoin.toRejoinToken
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

/**
 * @author Subham
 * @since 6/17/25
 */
@AutoRegister
object RejoinCommand : ScalaCommand()
{
    @CommandAlias("rejoin")
    fun onRejoin(player: Player, @Optional ignore: String?) = CompletableFuture.runAsync {
        val rejoinToken = player.uniqueId.toRejoinToken()
            ?: throw ConditionFailedException(
                "You are not able to rejoin a game at this time!"
            )

        if (ignore == "ignore")
        {
            player.uniqueId.destroyRejoinToken()
            player.sendMessage("${CC.RED}You chose to ignore your rejoin token!")
            return@runAsync
        }

        if (System.currentTimeMillis() > rejoinToken.expiration)
        {
            throw ConditionFailedException(
                "You tried to rejoin the game too late."
            )
        }

        player.sendMessage("${CC.GREEN}Attempting to send you to ${rejoinToken.server}...")

        VelocityRedirectSystem.redirect(
            player,
            rejoinToken.server,
            mapOf("rejoin" to Serializers.gson.toJson(rejoinToken))
        )
    }
}
