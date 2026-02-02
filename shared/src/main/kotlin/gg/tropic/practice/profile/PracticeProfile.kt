package gg.tropic.practice.profile

import gg.scala.commons.graduation.Progressive
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.storable.IDataStoreObject
import gg.scala.store.storage.type.DataStoreStorageType
import gg.tropic.practice.kit.Kit
import gg.tropic.practice.profile.loadout.Loadout
import gg.tropic.practice.profile.ranked.RankedBan
import gg.tropic.practice.statistics.Statistic
import gg.tropic.practice.statistics.StatisticID
import gg.tropic.practice.statistics.StatisticService
import io.lettuce.core.ScoredValue
import me.lucko.helper.Schedulers
import net.evilblock.cubed.util.time.Duration
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author GrowlyX
 * @since 9/17/2023
 */
data class PracticeProfile(
    override val identifier: UUID,
    override var matured: Set<String>? = setOf()
) : IDataStoreObject, Progressive
{
    private var rankedBan: RankedBan? = null

    @Transient
    var immutableMode: Boolean? = null

    fun inImmutableMode() = immutableMode == true

    @Transient
    var cachedStatistics: MutableMap<String, ScoredValue<Long>>? = ConcurrentHashMap()

    @Transient
    var trackedStatisticChangeSet: MutableSet<String> = mutableSetOf()

    @Transient
    var trackedStatisticChangeMutations: MutableMap<Statistic, Statistic.() -> Unit> = mutableMapOf()

    fun track(statistic: String)
    {
        trackedStatisticChangeSet += statistic
    }

    fun track(statistic: Statistic, use: Statistic.() -> Unit)
    {
        trackedStatisticChangeMutations[statistic] = use
    }

    fun getCachedStatisticValueWithDeferredEnqueue(id: String) = cachedStatistics?.get(id)
        .let {
            if (it == null)
            {
                Schedulers
                    .async()
                    .run {
                        StatisticService.statisticBy(id)?.scoreAndPosition()
                            ?.apply {
                                if (cachedStatistics == null)
                                {
                                    cachedStatistics = ConcurrentHashMap()
                                }

                                cachedStatistics!![id] = this
                            }
                    }
                return@let null
            }

            return@let it
        }

    fun getCachedStatisticValue(id: String) = cachedStatistics?.get(id)
    fun getStatisticValue(id: String) = cachedStatistics?.get(id)
        ?: StatisticService.statisticBy(id)?.scoreAndPosition()
            ?.apply {
                if (cachedStatistics == null)
                {
                    cachedStatistics = ConcurrentHashMap()
                }

                cachedStatistics!![id] = this
            }

    fun getCachedStatisticValueWithDeferredEnqueue(id: StatisticID) = getCachedStatisticValueWithDeferredEnqueue(id.toId())
    fun getCachedStatisticValue(id: StatisticID) = getCachedStatisticValue(id.toId())
    fun getStatisticValue(id: StatisticID) = getStatisticValue(id.toId())

    private var statistics: ConcurrentHashMap<String, Long>? = ConcurrentHashMap<String, Long>()
    fun statistics(): ConcurrentHashMap<String, Long>
    {
        if (statistics == null)
        {
            statistics = ConcurrentHashMap<String, Long>()
        }

        return statistics!!
    }

    val customLoadouts = ConcurrentHashMap<String, MutableList<Loadout>>()

    fun getLoadoutsFromKit(kit: Kit) = customLoadouts[kit.id] ?: mutableListOf()

    fun hasActiveRankedBan() = rankedBan != null && rankedBan!!.isEffective()
    fun applyRankedBan(duration: Duration)
    {
        val banDuration = if (duration.isPermanent()) null else System.currentTimeMillis() + duration.get()

        rankedBan = RankedBan(
            effectiveUntil = banDuration
        )
    }

    fun deliverRankedBanMessage(player: Player) = rankedBan!!.deliverBanMessage(player)

    fun rankedBanEffectiveUntil() = rankedBan!!.effectiveUntil
    fun removeRankedBan()
    {
        rankedBan = null
    }

    fun save() = DataStoreObjectControllerCache
        .findNotNull<PracticeProfile>()
        .save(this, DataStoreStorageType.MONGO)

}
