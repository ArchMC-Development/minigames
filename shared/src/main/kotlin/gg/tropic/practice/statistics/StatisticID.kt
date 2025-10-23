package gg.tropic.practice.statistics

import gg.tropic.practice.kit.Kit
import gg.tropic.practice.kit.KitService
import gg.tropic.practice.namespace
import gg.tropic.practice.queue.QueueType

/**
 * @author Subham
 * @since 6/22/25
 */
data class StatisticID(
    val type: String,
    val kit: String? = null,
    val queueType: QueueType? = null,
    val lifetime: StatisticLifetime? = null,
    var custom: String? = null
)
{
    companion object
    {
        @JvmStatic
        @JvmOverloads
        fun fromCustom(id: String, lifetime: StatisticLifetime? = null) = StatisticID(
            type = "custom",
            custom = id,
            lifetime = lifetime
        )

        fun fromExpected(id: String): StatisticID
        {
            if (id.startsWith("custom:"))
            {
                return fromCustom(
                    id.removePrefix("custom:"),
                    lifetime = when (true)
                    {
                        id.contains("daily") -> StatisticLifetime.Daily
                        id.contains("weekly") -> StatisticLifetime.Weekly
                        else -> null
                    }
                )
            }

            return from(id)
        }

        @JvmStatic
        fun from(id: String): StatisticID
        {
            val parts = id.split(":")
            if (parts.size != 4) {
                throw IllegalArgumentException("Invalid StatisticID format. Expected 4 parts separated by ':', got ${parts.size}")
            }

            val type = parts[0]
            val kit = if (parts[1] == "global") null else parts[1]
            val queueType = if (parts[2] == "global") null else QueueType.valueOf(parts[2].replaceFirstChar { it.uppercaseChar() })
            val lifetime = if (parts[3] == "lifetime") null else StatisticLifetime.valueOf(parts[3].replaceFirstChar { it.uppercaseChar() })

            return StatisticID(
                type = type,
                kit = kit,
                queueType = queueType,
                lifetime = lifetime
            )
        }
    }

    fun isCustom() = type == "custom" && custom != null

    fun toId(): String
    {
        if (isCustom())
        {
            return "custom:$custom"
        }

        val builder = StringBuilder(type)
        if (kit != null)
        {
            builder.append(":$kit")
        } else
        {
            builder.append(":global")
        }

        if (queueType != null)
        {
            builder.append(":${queueType.name.lowercase()}")
        } else
        {
            builder.append(":global")
        }

        if (lifetime != null)
        {
            builder.append(":${lifetime.name.lowercase()}")
        } else
        {
            builder.append(":lifetime")
        }

        return builder.toString()
    }

    fun toRedisKey(): String
    {
        if (lifetime == null)
        {
            return "${namespace()}:playerstats:${toId()}"
        }

        return "${namespace()}:playerstats:${lifetime.transform(toId())}"
    }
}

class StatisticReferenceDSL(
    private val type: String
)
{
    private var kit: String? = null
    private var queueType: QueueType? = null
    private var lifetime: StatisticLifetime? = null

    fun kit(kit: Kit?)
    {
        this.kit = kit?.id
    }

    fun kit(kit: String?)
    {
        this.kit = kit
    }

    fun queueType(queueType: QueueType)
    {
        this.queueType = queueType
    }

    fun casual() = queueType(QueueType.Casual)
    fun ranked() = queueType(QueueType.Ranked)

    fun daily() = lifetime(StatisticLifetime.Daily)
    fun weekly() = lifetime(StatisticLifetime.Weekly)

    fun lifetime(lifetime: StatisticLifetime?)
    {
        this.lifetime = lifetime
    }

    fun toReference() = StatisticID(
        type = type,
        kit = kit,
        queueType = queueType,
        lifetime = lifetime
    )
}

fun statisticId(id: String, dsl: StatisticReferenceDSL.() -> Unit): StatisticID
{
    val referenceDsl = StatisticReferenceDSL(id)
    referenceDsl.dsl()

    return referenceDsl.toReference()
}

fun statisticIdFrom(statistic: TrackedKitStatistic, dsl: StatisticReferenceDSL.() -> Unit = { }): StatisticID
{
    val referenceDsl = StatisticReferenceDSL(statistic.name.lowercase())
    referenceDsl.dsl()

    return referenceDsl.toReference()
}

class StatisticCompositionDSL
{
    private val kits = mutableListOf<String?>()
    private val queueTypes = mutableListOf<QueueType?>()
    private val types = mutableListOf<String>()
    private val lifetimes = mutableListOf<StatisticLifetime?>()

    fun kits(vararg kits: Kit?)
    {
        this.kits.addAll(kits.map { it?.id })
    }

    fun kits(vararg kits: String?)
    {
        this.kits.addAll(kits)
    }

    fun allServerKits()
    {
        allKits(KitService.cached().kits.values.toList())
    }

    fun allKits(availableKits: List<Kit>)
    {
        this.kits.addAll(availableKits.map { it.id })
        this.kits.add(null) // Add global kit option
    }

    fun globalKit()
    {
        this.kits.add(null)
    }

    fun queueTypes(vararg queueTypes: QueueType?)
    {
        this.queueTypes.addAll(queueTypes)
    }

    fun allQueueTypes()
    {
        this.queueTypes.addAll(QueueType.entries)
        this.queueTypes.add(null) // Add global queue type option
    }

    fun globalQueueType()
    {
        this.queueTypes.add(null)
    }

    fun casual()
    {
        this.queueTypes.add(QueueType.Casual)
    }

    fun ranked()
    {
        this.queueTypes.add(QueueType.Ranked)
    }

    fun allTypes()
    {
        this.types.addAll(TrackedKitStatistic.entries.map { it.name.lowercase() })
    }

    fun types(vararg statistics: TrackedKitStatistic)
    {
        this.types.addAll(statistics.map { it.name.lowercase() })
    }

    fun types(vararg typeNames: String)
    {
        this.types.addAll(typeNames)
    }

    fun lifetimes(vararg lifetimes: StatisticLifetime?)
    {
        this.lifetimes.addAll(lifetimes)
    }

    fun allLifetimes()
    {
        this.lifetimes.addAll(StatisticLifetime.entries)
        this.lifetimes.add(null) // Add lifetime option
    }

    fun daily()
    {
        this.lifetimes.add(StatisticLifetime.Daily)
    }

    fun weekly()
    {
        this.lifetimes.add(StatisticLifetime.Weekly)
    }

    fun lifetime()
    {
        this.lifetimes.add(null)
    }

    fun build(): List<StatisticID>
    {
        val result = mutableListOf<StatisticID>()

        // If no kits specified, use global
        val kitList = if (kits.isEmpty()) listOf(null) else kits

        // If no queue types specified, use global
        val queueTypeList = if (queueTypes.isEmpty()) listOf(null) else queueTypes

        // If no lifetimes specified, use lifetime (null)
        val lifetimeList = if (lifetimes.isEmpty()) listOf(null) else lifetimes

        // Generate all combinations
        for (type in types)
        {
            for (kit in kitList)
            {
                for (queueType in queueTypeList)
                {
                    for (lifetime in lifetimeList)
                    {
                        result.add(
                            StatisticID(
                                type = type,
                                kit = kit,
                                queueType = queueType,
                                lifetime = lifetime
                            )
                        )
                    }
                }
            }
        }

        return result
    }
}

fun statisticIds(dsl: StatisticCompositionDSL.() -> Unit): List<StatisticID>
{
    val compositionDsl = StatisticCompositionDSL()
    compositionDsl.dsl()
    return compositionDsl.build()
}
