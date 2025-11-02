package gg.tropic.practice.extensions

import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * @author Subham
 * @since 6/28/25
 */
fun createProgressBar(progress: Double, total: Double): String
{
    if (progress > total) return "${CC.GREEN}${CC.STRIKE_THROUGH} ".repeat(18)

    val segments = 18
    val progressRatio = (progress / total).coerceIn(0.0, 1.0)
    val completedSegments = (segments * progressRatio).toInt()

    val completedPart = "${CC.GREEN}${CC.STRIKE_THROUGH}${" ".repeat(completedSegments)}"
    val remainingPart = "${CC.GRAY}${CC.STRIKE_THROUGH}${" ".repeat(segments - completedSegments)}"

    return "$completedPart$remainingPart"
}

@OptIn(ExperimentalEncodingApi::class)
val bar = "█"

fun createProgressBarAlt(color: String, progress: Double, total: Double): String
{
    if (progress > total) return "$color${CC.STRIKE_THROUGH}$bar".repeat(18)

    val segments = 10
    val progressRatio = (progress / total).coerceIn(0.0, 1.0)
    val completedSegments = (segments * progressRatio).toInt()

    val completedPart = "${color}${bar.repeat(completedSegments)}"
    val remainingPart = "${CC.GRAY}${bar.repeat(segments - completedSegments)}"

    return "$completedPart$remainingPart"
}

fun createProgressBarAlt2(color: String, progress: Double, total: Double): String
{
    if (progress > total) return "$color${CC.STRIKE_THROUGH}${Constants.THICK_VERTICAL_LINE}".repeat(18)

    val segments = 10
    val progressRatio = (progress / total).coerceIn(0.0, 1.0)
    val completedSegments = (segments * progressRatio).toInt()

    val completedPart = "${color}${"${Constants.THICK_VERTICAL_LINE}".repeat(completedSegments)}"
    val remainingPart = "${CC.GRAY}${"${Constants.THICK_VERTICAL_LINE}".repeat(segments - completedSegments)}"

    return "$completedPart$remainingPart"
}
