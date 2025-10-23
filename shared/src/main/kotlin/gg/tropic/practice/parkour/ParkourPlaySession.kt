package gg.tropic.practice.parkour

/**
 * @author GrowlyX
 * @since 3/18/2025
 */
data class ParkourPlaySession(
    val start: Long = System.currentTimeMillis(),
    val checkpointTimes: MutableMap<Int, Long> = mutableMapOf()
)
