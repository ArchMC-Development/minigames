package gg.tropic.practice.queue

import gg.tropic.practice.kit.Kit
import kotlin.properties.Delegates

/**
 * @author GrowlyX
 * @since 8/10/2024
 */
class DSLQueueID
{
    private var kitID: String? = null
    private lateinit var queueType: QueueType
    private var teamSize by Delegates.notNull<Int>()

    fun kit(kit: Kit) = apply {
        kitID = kit.id
    }

    fun kit(kit: String) = apply {
        kitID = kit
    }

    fun teamSize(size: Int) = apply {
        teamSize = size
    }

    fun queueType(queueType: QueueType) = apply {
        this.queueType = queueType
    }

    fun compose() = "${if (kitID == null) "" else "$kitID:"}$queueType:${teamSize}v$teamSize"
}

fun queueId(dsl: DSLQueueID.() -> Unit) = DSLQueueID().apply(dsl).compose()

data class ParsedQueueID(
    val kitID: String?,
    val queueType: QueueType,
    val teamSize: Int
)

class QueueIDParser
{
    companion object
    {
        fun parseDetailed(queueID: String) = queueID.split(":").let { split ->
            if (split.size == 2)
            {
                return@let ParsedQueueID(
                    null,
                    QueueType.valueOf(split[0]),
                    split[1].split("v").first().toInt()
                )
            }

            ParsedQueueID(
                split[0],
                QueueType.valueOf(split[1]),
                split[2].split("v").first().toInt()
            )
        }
    }
}

fun String.toQueueIDComponents(): ParsedQueueID? = QueueIDParser.parseDetailed(this)
