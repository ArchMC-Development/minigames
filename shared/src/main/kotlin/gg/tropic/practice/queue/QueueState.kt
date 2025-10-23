package gg.tropic.practice.queue

import gg.scala.commons.ScalaCommons
import net.evilblock.cubed.serializers.Serializers
import java.util.UUID

/**
 * @author GrowlyX
 * @since 9/24/2023
 */
data class QueueState(
    val kitId: String?,
    val queueType: QueueType,
    val teamSize: Int,
    val entry: QueueEntry
)

fun UUID.toQueueState() = ScalaCommons.bundle().globals().redis()
    .sync()
    .hget(
        "$queueV2Namespace:states",
        this.toString()
    )
    ?.let { Serializers.gson.fromJson(it, QueueState::class.java) }

fun totalPlayersQueued() = ScalaCommons.bundle().globals().redis()
    .sync()
    .hlen("$queueV2Namespace:states")
