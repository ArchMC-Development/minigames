package gg.tropic.practice.map.metadata

import kotlin.math.absoluteValue
import kotlin.math.sign

fun Double.toSanitizedCoordinate(): Double
{
    val sign = sign
    val intPart = toInt().absoluteValue
    val decimalPart = this.absoluteValue - intPart

    return sign * when
    {
        decimalPart < 0.25 -> intPart.toDouble()
        decimalPart < 0.75 -> intPart + 0.5
        else -> intPart + 1.0
    }
}
