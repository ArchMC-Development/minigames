package gg.tropic.practice.metadata

import gg.scala.commons.ScalaCommons
import net.evilblock.cubed.serializers.Serializers
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * @author GrowlyX
 * @since 3/28/2025
 */
class MetadataReader
{
    private var bulk = mapOf<String, String>()
    private val updateLock = ReentrantReadWriteLock()

    private var namespace = "tropicpractice"

    fun withNamespace(namespace: String) = apply {
        this.namespace = namespace
    }

    fun read(key: String) = bulk[key]
    fun bulk() = updateLock.read { bulk }

    fun enableAutomatedPull(service: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor())
    {
        service.scheduleAtFixedRate(::pull, 0L, 500L, TimeUnit.MILLISECONDS)
    }

    fun pull()
    {
        updateLock.write {
            ScalaCommons.bundle().globals().redis().sync()
                .get("$namespace:metadata:bulk")
                ?.let {
                    Serializers.gson.fromJson(it, BulkMetadata::class.java)
                }
                ?.let {
                    bulk = it.metadata
                }
        }
    }
}
