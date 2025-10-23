package gg.tropic.practice.queue

import gg.tropic.practice.games.manager.GameManager
import gg.tropic.practice.metadata.Metadata
import gg.tropic.practice.persistence.RedisShared
import gg.tropic.practice.region.Region
import lol.arch.symphony.api.Symphony
import java.util.Date
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.math.max
import kotlin.math.min

/**
 * @author Subham
 * @since 6/15/25
 */
class CentralSubscribablePlayerQueueHolder : Runnable
{
    val playerQueues = ConcurrentHashMap<String, SubscribablePlayerQueue>()
    fun trackPlayerQueue(playerQueue: SubscribablePlayerQueue)
    {
        playerQueues[playerQueue.id] = playerQueue
    }

    fun isHolding(queueId: String) = playerQueues.containsKey(queueId)

    fun forgetPlayerQueue(id: String)
    {
        playerQueues.remove(id)
    }

    fun subscribe(queueID: String, entry: QueueEntry)
    {
        if (queueOfPlayer(entry.leader) != null)
        {
            queueOfPlayer(entry.leader)?.first?.unsubscribe(entry)
        }

        playerQueues[queueID]?.subscribe(entry)
    }

    fun unsubscribe(player: UUID)
    {
        queueOfPlayer(player)?.apply {
            first.unsubscribe(second.data)
        }
    }

    fun onProcessAll()
    {
        playerQueues.values.forEach { queue ->
            queue.processAndDestroy()
        }
    }

    fun queueOfPlayer(player: UUID) = playerQueues.values
        .firstNotNullOfOrNull { queue ->
            queue.internalQueue.listElements()
                .firstOrNull { entry -> player in entry.data.players }
                ?.let { entry ->
                    queue to entry
                }
        }

    fun configure(executor: ScheduledExecutorService)
    {
        thread(
            start = true,
            block = ::run,
            isDaemon = true
        )

        executor.scheduleAtFixedRate(::onExpansionUpdate, 0L, 1000L, TimeUnit.MILLISECONDS)
        executor.scheduleAtFixedRate(::onMetadataUpdate, 0L, 500L, TimeUnit.MILLISECONDS)

        Symphony
            .createEventSubscriber()
            .onLogout { player ->
                val presentInAnyQueue = queueOfPlayer(player)
                presentInAnyQueue?.apply {
                    RedisShared.sendMessage(
                        second.data.players,
                        listOf(
                            "&c&lYou were removed from the queue as a player in your group disconnected from the server!"
                        )
                    )

                    first.unsubscribe(second.data)
                }
            }
            .subscribe()
            .join()
    }

    fun onMetadataUpdate()
    {
        val games = GameManager.allGames()
        playerQueues.values.forEach { queue ->
            val gamesMatchingQueueID = games.filter { it.queueId == queue.id }
            val playersInGame = gamesMatchingQueueID.sumOf { it.onlinePlayers ?: it.players.size }

            Metadata.writer().write(
                "queue:users-queued:${queue.id}",
                queue.internalQueue.size().toString()
            )

            Metadata.writer().write(
                "queue:users-playing:${queue.id}",
                playersInGame.toString()
            )
        }
    }

    fun onExpansionUpdate()
    {
        playerQueues.values.forEach { queue ->
            queue.internalQueue.listElements().forEach { queueEntry ->
                val entry = queueEntry.data
                var requiresUpdates = false

                val rangeExpansionUpdates = { time: Long ->
                    System.currentTimeMillis() >= time + 1500L
                }

                if (queue.toQueueIDComponents()?.queueType == QueueType.Ranked)
                {
                    if (rangeExpansionUpdates(entry.lastELORangeExpansion))
                    {
                        entry.leaderRangedELO.diffsBy = min(
                            2_000, // TODO is 2000 going to be the max ELO?
                            (entry.leaderRangedELO.diffsBy * 1.5).toInt()
                        )
                        entry.lastELORangeExpansion = System.currentTimeMillis()
                        requiresUpdates = true
                    }
                }

                val previousPingDiff = entry.leaderRangedPing.diffsBy
                if (entry.maxPingDiff != -1)
                {
                    if (entry.leaderRangedPing.diffsBy < entry.maxPingDiff)
                    {
                        if (rangeExpansionUpdates(entry.lastPingRangeExpansion))
                        {
                            entry.leaderRangedPing.diffsBy = min(
                                entry.maxPingDiff,
                                (entry.leaderRangedPing.diffsBy * 1.5).toInt()
                            )
                            entry.lastPingRangeExpansion = System.currentTimeMillis()
                            val differential = entry.leaderRangedPing.diffsBy - previousPingDiff
                            entry.lastRecordedDifferential = differential

                            requiresUpdates = true

                            val pingRange = entry.leaderRangedPing.toIntRangeInclusive()
                            RedisShared.sendMessage(
                                entry.players,
                                listOf(
                                    "{secondary}You are matchmaking in an ping range of ${
                                        "&a[${max(0, pingRange.first)} -> ${pingRange.last}]{secondary}"
                                    } &7(expanded by ±${
                                        differential
                                    }). ${
                                        if (entry.leaderRangedPing.diffsBy == entry.maxPingDiff) "&lThe range will no longer be expanded as it has reached its maximum of ±${entry.maxPingDiff}!" else ""
                                    }"
                                )
                            )
                        }
                    }
                }

                if (
                    entry.preferredQueueRegion != Region.Both &&
                    System.currentTimeMillis() - entry.joinQueueTimestamp >= 10_000L
                )
                {
                    requiresUpdates = true

                    val previousRegion = entry.preferredQueueRegion
                    entry.preferredQueueRegion = Region.Both

                    RedisShared.sendMessage(
                        entry.players,
                        listOf(
                            "{secondary}You are now matchmaking in the &aGlobal{secondary} queue as we could not find an opponent for you in the {primary}${previousRegion.name}{secondary} queue."
                        )
                    )
                }

                if (requiresUpdates)
                {
                    queue.updateStates(entry)
                }
            }
        }
    }

    override fun run()
    {
        while (true)
        {
            runCatching {
                onProcessAll()
            }.onFailure { throwable ->
                throwable.printStackTrace()
            }

            Thread.sleep(50L)
        }
    }
}
