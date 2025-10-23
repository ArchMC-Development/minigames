package gg.tropic.practice.parkour

/**
 * @author GrowlyX
 * @since 3/18/2025
 */
fun Long.formatDurationIntoTwoDecimal() =  "%.2f".format(((System.currentTimeMillis() - this) / 1000.0).toFloat())
fun Long.formatIntoTwoDecimal() =  "%.2f".format((this / 1000.0).toFloat())
