package gg.tropic.practice.command

import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.tropic.practice.games.GameService
import net.evilblock.cubed.util.CC
import org.bukkit.potion.PotionEffectType

/**
 * @author GrowlyX
 * @since 10/28/2023
 */
@AutoRegister
object NoSpeedCommand : ScalaCommand()
{
    @CommandAlias("nospeed")
    fun onNoSpeed(player: ScalaPlayer)
    {
        val game = GameService.byPlayer(player.bukkit())
        if (game != null && game.miniGameLifecycle != null)
        {
            throw ConditionFailedException(
                "You cannot use this command right now!"
            )
        }

        if (!player.bukkit().hasPotionEffect(PotionEffectType.SPEED))
        {
            throw ConditionFailedException("You do not have speed!")
        }

        player.bukkit().removePotionEffect(PotionEffectType.SPEED)
        player.sendMessage("${CC.RED}You no longer have speed!")
    }

    @CommandAlias("debuggame")
    @CommandPermission("op")
    fun onGameInfo(player: ScalaPlayer)
    {
        player.sendMessage(
            "Expected world: ${GameService.byPlayer(player.bukkit())?.arenaWorld?.name}",
            "Actual world: ${player.bukkit().world.name}"
        )
    }
}
