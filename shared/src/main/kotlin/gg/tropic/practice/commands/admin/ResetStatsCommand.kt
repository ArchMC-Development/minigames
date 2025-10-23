package gg.tropic.practice.commands.admin

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandCompletion
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Optional
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import gg.tropic.practice.kit.Kit
import gg.tropic.practice.profile.PracticeProfile
import gg.tropic.practice.statistics.TrackedKitStatistic
import gg.tropic.practice.statistics.StatisticService
import gg.tropic.practice.statistics.statisticWrite
import gg.tropic.practice.statistics.statisticIds
import net.evilblock.cubed.util.CC
import org.bukkit.command.CommandSender

/**
 * @author GrowlyX
 * @since 12/23/2023
 */
@AutoRegister
object ResetStatsCommand : ScalaCommand()
{
    enum class StatReset(
        val resetFor: PracticeProfile.(Kit?) -> Unit
    )
    {
        ELO({ kit ->
            statisticWrite(
                statisticIds {
                    if (kit == null)
                    {
                        allServerKits()
                    } else
                    {
                        kits(kit)
                    }

                    ranked()
                    allLifetimes()
                    types(TrackedKitStatistic.ELO)
                }
            ) {
                update(defaultValue)
            }
        }),
        CasualStats({ kit ->
            statisticWrite(
                statisticIds {
                    if (kit == null)
                    {
                        allServerKits()
                    } else
                    {
                        kits(kit)
                    }

                    casual()
                    allLifetimes()
                    allTypes()
                }
            ) {
                update(defaultValue)
            }
        }),
        RankedStats({ kit ->
            statisticWrite(
                statisticIds {
                    if (kit == null)
                    {
                        allServerKits()
                    } else
                    {
                        kits(kit)
                    }

                    ranked()
                    allLifetimes()
                    allTypes()
                }
            ) {
                update(defaultValue)
            }
        }),
        GlobalStats({ _ ->
            statisticWrite(
                statisticIds {
                    globalKit()
                    globalQueueType()
                    allLifetimes()
                    allTypes()
                }
            ) {
                update(defaultValue)
            }
        }),
        AllStats({ kit ->
            RankedStats.resetFor(this, kit)
            GlobalStats.resetFor(this, kit)
            CasualStats.resetFor(this, kit)
        })
    }

    @CommandAlias("resetstats")
    @CommandCompletion("@mip-players * @kits")
    @CommandPermission("practice.command.resetstats")
    fun onResetStats(
        player: CommandSender,
        target: AsyncLemonPlayer,
        resetOptions: StatReset,
        @Optional kit: Kit?
    ) = target.validatePlayers(player, false) {
        StatisticService
            .update(it.uniqueId) {
                resetOptions.resetFor(this, kit)
            }
            .thenRun {
                player.sendMessage(
                    "${CC.GREEN}You have reset the ${CC.BOLD}${
                        resetOptions.name
                    }${CC.GREEN} for ${CC.WHITE}${it.name}${CC.GREEN}${
                        if (kit != null) " on kit ${CC.PRI}${kit.id}${CC.GREEN}" else ""
                    }."
                )
            }
            .join()
    }
}
