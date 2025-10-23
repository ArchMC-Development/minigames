package gg.tropic.practice.commands.customizers

import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.annotations.commands.customizer.CommandManagerCustomizer
import gg.scala.commons.command.ScalaCommandManager
import gg.tropic.practice.statistics.Statistic
import gg.tropic.practice.statistics.StatisticService
import net.evilblock.cubed.util.CC

/**
 * @author Subham
 * @since 7/24/25
 */
object StatisticCommandCustomizers
{
    @CommandManagerCustomizer
    fun customize(manager: ScalaCommandManager)
    {
        manager.commandContexts.registerContext(Statistic::class.java) {
            val arg = it.popFirstArg()
            StatisticService.statisticBy(arg)
                ?: throw ConditionFailedException(
                    "No statistic by the id ${CC.YELLOW}$arg${CC.RED} exists."
                )
        }

        manager.commandCompletions.registerAsyncCompletion("statisticIds") {
            StatisticService.trackedStatistics()
                .map { it.id.toId() }
        }
        manager.commandCompletions.setDefaultCompletion("statisticIds", Statistic::class.java)
    }
}
