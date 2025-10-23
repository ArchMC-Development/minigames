package gg.tropic.practice.metadata

import gg.scala.commons.ScalaCommons
import net.evilblock.cubed.serializers.Serializers
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * @author GrowlyX
 * @since 3/28/2025
 */
class MetadataWriter
{
    private val updates = mutableMapOf<String, String>()
    private val bulk = mutableMapOf<String, String>()
    private val updateLock = ReentrantLock()

    private var namespace = "tropicpractice"

    fun withNamespace(namespace: String) = apply {
        this.namespace = namespace
    }

    fun write(key: String, value: String)
    {
        updateLock.withLock {
            updates[key] = value
        }
    }

    fun enableAutomatedPush()
    {
        val executor = Executors.newSingleThreadScheduledExecutor()
        executor.scheduleAtFixedRate(::push, 0L, 500L, TimeUnit.MILLISECONDS)
    }

    fun push()
    {
        updateLock.withLock {
            bulk.putAll(updates)
            updates.clear()

            ScalaCommons.bundle().globals().redis().sync()
                .setex(
                    "$namespace:metadata:bulk",
                    10,
                    Serializers.gson.toJson(BulkMetadata(bulk))
                )
        }
    }
}
