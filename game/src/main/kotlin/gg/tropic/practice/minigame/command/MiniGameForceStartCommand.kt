package gg.tropic.practice.minigame.command

import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.tropic.practice.games.GameService
import gg.tropic.practice.games.GameState
import gg.tropic.practice.games.event.GameStartEvent
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 8/25/2024
 */
@AutoRegister
object MiniGameForceStartCommand : ScalaCommand()
{
    @CommandAlias("forcestart")
    @CommandPermission("minigame.admin")
    fun start(player: Player)
    {
        val game = GameService.byPlayer(player)
            ?: throw ConditionFailedException("You are not in a game!")

        if (!game.state(GameState.Waiting))
        {
            throw ConditionFailedException("You cannot do this right now!")
        }

        if (game.miniGameLifecycle == null)
        {
            throw ConditionFailedException("This game is not a candidate for Force Start!")
        }

        val event = GameStartEvent(game)
        Bukkit.getPluginManager().callEvent(event)

        if (event.isCancelled)
        {
            game.state = GameState.Completed
            game.closeAndCleanup()

            player.sendMessage("${CC.RED}Failed to force start!")
            return
        }

        game.state = GameState.Playing
        game.startTimestamp = System.currentTimeMillis()

        player.sendMessage("${CC.GREEN}Force started!")
    }
}
