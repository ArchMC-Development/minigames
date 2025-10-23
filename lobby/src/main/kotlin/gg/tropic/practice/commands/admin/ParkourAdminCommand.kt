package gg.tropic.practice.commands.admin

import gg.scala.commons.ScalaCommons
import gg.scala.commons.acf.CommandHelp
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandCompletion
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Default
import gg.scala.commons.acf.annotation.Description
import gg.scala.commons.acf.annotation.HelpCommand
import gg.scala.commons.acf.annotation.Subcommand
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import gg.tropic.practice.parkour.ParkourService
import net.evilblock.cubed.util.CC

/**
 * @author GrowlyX
 * @since 3/18/2025
 */
@AutoRegister
@CommandPermission("practice.command.manageparkour")
@CommandAlias("manageparkour")
object ParkourAdminCommand : ScalaCommand()
{
    @Default
    @HelpCommand
    fun onDefault(help: CommandHelp)
    {
        help.showHelp()
    }

    @CommandCompletion("@mip-players")
    @Subcommand("removelbentry")
    @Description("Remove a player from the Parkour leaderboards.")
    fun removeLeaderboardEntry(
        player: ScalaPlayer,
        target: AsyncLemonPlayer
    ) = target.validatePlayers(player.bukkit(), false) {
        ScalaCommons.bundle().globals().redis().sync()
            .zrem(
                ParkourService.redisKey(),
                it.uniqueId.toString()
            )

        player.sendMessage("${CC.GREEN}${it.name}'s parkour leaderboard entry has been removed.")
    }
}
