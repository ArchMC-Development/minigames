package gg.tropic.practice.statistics

import gg.tropic.practice.profile.PracticeProfile
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.math.Numbers

/**
* @author Subham
* @since 6/22/25
*/
fun <T> statistic(id: StatisticID, use: Statistic.() -> T) = StatisticService.statistic(id.toId(), use)
fun <T> statistic(vararg id: StatisticID, use: Statistic.() -> T) = id.forEach { StatisticService.statistic(it.toId(), use) }
fun <T> statistic(ids: List<StatisticID>, use: Statistic.() -> T) = ids.forEach { StatisticService.statistic(it.toId(), use) }

fun PracticeProfile.statisticWrite(id: StatisticID, use: Statistic.() -> Unit) = StatisticService.trackStatistic(this, id.toId(), use)
fun PracticeProfile.statisticWrite(vararg id: StatisticID, use: Statistic.() -> Unit) = id.forEach { StatisticService.trackStatistic(this, it.toId(), use) }
fun PracticeProfile.statisticWrite(ids: List<StatisticID>, use: Statistic.() -> Unit) = ids.forEach { StatisticService.trackStatistic(this, it.toId(), use) }

fun <T> PracticeProfile.statisticRead(id: StatisticID, use: Statistic.() -> T) = StatisticService.statistic(id.toId(), use)
fun <T> PracticeProfile.statisticRead(vararg id: StatisticID, use: Statistic.() -> T) = id.forEach { StatisticService.statistic(it.toId(), use) }
fun <T> PracticeProfile.statisticRead(ids: List<StatisticID>, use: Statistic.() -> T) = ids.forEach { StatisticService.statistic(it.toId(), use) }

fun PracticeProfile.valueOf(statisticID: StatisticID) = getStatisticValue(statisticID)
    ?.let {
        "${CC.WHITE}${
            Numbers.format(it.score.toLong())
        } ${
            if (it.value != -1L) "${CC.GRAY}(#${
                Numbers.format(it.value + 1)
            })" else ""
        }"
    }
    ?: "???"

fun PracticeProfile.valueOfReset(statisticID: StatisticID) = getStatisticValue(statisticID)
    ?.let {
        "${
            Numbers.format(it.score.toLong())
        } ${
            if (it.value != -1L) "${CC.GRAY}(#${
                Numbers.format(it.value + 1)
            })" else ""
        }"
    }
    ?: "???"

fun PracticeProfile.numericalValueOf(statisticID: StatisticID) = getStatisticValue(statisticID)
