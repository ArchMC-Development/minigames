package gg.tropic.practice.commands.admin

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
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import gg.tropic.practice.statistics.Statistic
import gg.tropic.practice.statistics.StatisticID
import gg.tropic.practice.statistics.StatisticService
import gg.tropic.practice.statistics.statisticWrite
import net.evilblock.cubed.util.CC
import org.bukkit.command.CommandSender

/**
 * @author Subham
 * @since 7/24/25
 */
@AutoRegister
@CommandAlias("statistic")
@CommandPermission("practice.command.statistic")
object StatisticCommand : ScalaCommand()
{
    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp) = help.showHelp()

    @CommandCompletion("@players @statisticIds")
    @Subcommand("add")
    @Description("Set a value of a statistic.")
    fun onAdd(
        player: CommandSender,
        target: AsyncLemonPlayer,
        statistic: Statistic,
        quantity: Long
    ) = target.validatePlayers(player, true) {
        StatisticService
            .update(it.uniqueId) {
                statisticWrite(statistic.id) {
                    add(quantity)
                }
            }
            .thenAccept { changes ->
                player.sendMessage("${CC.GREEN}Changes made:")
                changes.forEach { (id, old, new, _) ->
                    player.sendMessage("${CC.WHITE}- $id: $old -> $new")
                }
            }
            .join()
    }

    @CommandCompletion("@players @statisticIds")
    @Subcommand("set")
    @Description("Set a value of a statistic.")
    fun onSet(
        player: CommandSender,
        target: AsyncLemonPlayer,
        statistic: Statistic,
        quantity: Long
    ) = target.validatePlayers(player, true) {
        StatisticService
            .update(it.uniqueId) {
                statisticWrite(statistic.id) {
                    update(quantity)
                }
            }
            .thenAccept { changes ->
                player.sendMessage("${CC.GREEN}Changes made:")
                changes.forEach { (id, old, new, _) ->
                    player.sendMessage("${CC.WHITE}- $id: $old -> $new")
                }
            }
            .join()
    }

    @CommandCompletion("@players @statisticIds")
    @Subcommand("reset")
    @Description("Reset a value of a statistic.")
    fun onSetDefault(
        player: CommandSender,
        target: AsyncLemonPlayer,
        statistic: Statistic
    ) = target.validatePlayers(player, true) {
        StatisticService
            .update(it.uniqueId) {
                statisticWrite(statistic.id) {
                    update(defaultValue)
                }
            }
            .thenAccept { changes ->
                player.sendMessage("${CC.GREEN}Changes made:")
                changes.forEach { (id, old, new, _) ->
                    player.sendMessage("${CC.WHITE}- $id: $old -> $new")
                }
            }
            .join()
    }
}
