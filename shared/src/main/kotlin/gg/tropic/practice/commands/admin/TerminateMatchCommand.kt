package gg.tropic.practice.commands.admin

import gg.scala.aware.message.AwareMessage
import gg.scala.aware.thread.AwareThreadContext
import gg.scala.commons.ScalaCommons
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandCompletion
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Optional
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.playerstatus.canVirtuallySee
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import gg.scala.lemon.util.QuickAccess
import gg.tropic.practice.games.GameState
import gg.tropic.practice.metadata.SystemMetadataService
import gg.tropic.practice.suffixWhenDev
import net.evilblock.cubed.util.CC
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 12/24/2023
 */
@AutoRegister
object TerminateMatchCommand : ScalaCommand()
{
    @CommandAlias("terminatematch")
    @CommandCompletion("@mip-players")
    @CommandPermission("practice.command.terminatematch")
    fun onTerminateMatch(player: CommandSender, target: AsyncLemonPlayer, @Optional reason: String?) = target
        .validatePlayers(player, false) {
            val online = QuickAccess
                .online(it.uniqueId)
                .join()

            if (!online || (player is Player && !player.canVirtuallySee(it.uniqueId)))
            {
                throw ConditionFailedException(
                    "The player ${CC.YELLOW}${it.name} is not logged onto the network."
                )
            }

            SystemMetadataService.allGames()
                .firstOrNull { game -> it.uniqueId in game.players }
                .let { reference ->
                    if (reference == null)
                    {
                        throw ConditionFailedException(
                            "The player ${CC.YELLOW}${it.name}${CC.RED} is not currently in a match."
                        )
                    }

                    if (reference.state != GameState.Playing)
                    {
                        throw ConditionFailedException(
                            "You are unable to terminate ${CC.YELLOW}${it.name}'s${CC.RED} match when it is in the ${CC.B_RED}${reference.state.name}${CC.RED} state."
                        )
                    }

                    AwareMessage
                        .of(
                            "terminate",
                            ScalaCommons.bundle().globals().aware(),
                            "server" to reference.server,
                            "matchID" to reference.uniqueId,
                            "terminator" to if (player is Player) player.uniqueId else null,
                            "reason" to reason
                        )
                        .publish(
                            AwareThreadContext.SYNC,
                            channel = "practice-matchtermination".suffixWhenDev()
                        )

                    player.sendMessage("${CC.GREEN}Sent the match termination request!")
                }
        }
}
