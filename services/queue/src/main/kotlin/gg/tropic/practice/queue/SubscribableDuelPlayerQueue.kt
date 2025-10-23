package gg.tropic.practice.queue

import gg.tropic.practice.application.api.defaults.kit.ImmutableKit
import gg.tropic.practice.application.api.defaults.map.MapDataSync
import gg.tropic.practice.expectation.GameExpectation
import gg.tropic.practice.games.team.GameTeam
import gg.tropic.practice.games.team.TeamIdentifier
import gg.tropic.practice.persistence.RedisShared
import gg.tropic.practice.region.Region
import java.util.UUID

/**
 * @author Subham
 * @since 6/15/25
 */
class SubscribableDuelPlayerQueue(
    private val kit: ImmutableKit,
    private val teamSize: Int,
    private val queueType: QueueType,
    override val id: String = queueId {
        kit(kit.id)
        queueType(queueType)
        teamSize(teamSize)
    },
    override val internalQueue: InternalQueue<QueueEntry> = InternalQueue()
) : SubscribablePlayerQueue
{
    override fun onProcess(): List<QueueEntry>
    {
        val length = internalQueue.size()
        if (length < teamSize * 2)
        {
            return listOf()
        }

        // don't unnecessarily load in and map to data class if not needed
        val first: List<QueueEntry>
        val second: List<QueueEntry>

        // Faster than doing a list intersect which compares all items
        fun IntRange.quickIntersect(other: IntRange): Boolean
        {
            return this.first <= other.last && this.last >= other.first
        }

        val queueEntries = playersInQueue()
        val groupedQueueEntries = if (queueType == QueueType.Ranked)
        {
            queueEntries
                .map { entry ->
                    val otherEntriesMatchingEntry = queueEntries
                        .filter { otherEntry ->
                            val doesELOIntersect = entry.data.leaderRangedELO.toIntRangeInclusive()
                                .quickIntersect(
                                    otherEntry.data.leaderRangedELO
                                        .toIntRangeInclusive()
                                )

                            // we can ignore ping intersections if they have no ping restriction
                            val doesPingIntersect =
                                (entry.data.maxPingDiff == -1 || otherEntry.data.maxPingDiff == -1) ||
                                    entry.data.leaderRangedPing.toIntRangeInclusive()
                                        .quickIntersect(
                                            otherEntry.data.leaderRangedPing
                                                .toIntRangeInclusive()
                                        )

                            entry != otherEntry &&
                                doesELOIntersect &&
                                doesPingIntersect &&
                                ((entry.data.queueRegion == otherEntry.data.queueRegion) ||
                                    (entry.data.preferredQueueRegion == otherEntry.data.preferredQueueRegion))
                        }

                    otherEntriesMatchingEntry + entry
                }
                .filter {
                    it.size >= teamSize * 2
                }
        } else
        {
            queueEntries
                .map { entry ->
                    val otherEntriesMatchingEntry = queueEntries
                        .filter { otherEntry ->
                            // we can ignore ping intersections if they have no ping restriction
                            val doesPingIntersect =
                                (entry.data.maxPingDiff == -1 || otherEntry.data.maxPingDiff == -1) ||
                                    entry.data.leaderRangedPing.toIntRangeInclusive()
                                        .quickIntersect(
                                            otherEntry.data.leaderRangedPing
                                                .toIntRangeInclusive()
                                        )

                            entry != otherEntry &&
                                ((entry.data.queueRegion == otherEntry.data.queueRegion) ||
                                    (entry.data.preferredQueueRegion == otherEntry.data.preferredQueueRegion)) &&
                                doesPingIntersect
                        }

                    otherEntriesMatchingEntry + entry
                }
                .filter {
                    it.size >= teamSize * 2
                }
        }

        if (groupedQueueEntries.isEmpty())
        {
            return listOf()
        }

        val selectedQueueMatch = groupedQueueEntries.random()
            .map { it.data }

        // Expecting a symmetric list here, lets hope it doesn't break?
        first = selectedQueueMatch.take(teamSize)
        second = selectedQueueMatch.takeLast(teamSize)

        if (first.size != teamSize || second.size != teamSize)
        {
            return listOf()
        }

        val firstPlayers = first.flatMap { it.players }
        val secondPlayers = second.flatMap { it.players }
        val users = listOf(firstPlayers, secondPlayers).flatten()

        val map = MapDataSync
            .selectRandomMapCompatibleWith(kit)
            ?: return run {
                RedisShared.sendMessage(
                    users,
                    listOf(
                        "&c&lWe found no map compatible with the kit you are queueing for!"
                    )
                )
                selectedQueueMatch
            }

        val expectation = GameExpectation(
            identifier = UUID.randomUUID(),
            players = listOf(first, second)
                .flatMap {
                    it.flatMap { entry ->
                        entry.players
                    }
                }
                .toMutableSet(),
            teams = setOf(
                GameTeam(teamIdentifier = TeamIdentifier.A, players = firstPlayers.toMutableSet()),
                GameTeam(teamIdentifier = TeamIdentifier.B, players = secondPlayers.toMutableSet())
            ),
            kitId = kit.id,
            mapId = map.name,
            queueType = queueType,
            queueId = id
        )

        val region = first.first().preferredQueueRegion
        GameQueueManager
            .prepareGameFor(
                map = map,
                expectation = expectation,
                // prefer NA servers if queuing globally
                region = if (region == Region.Both) Region.NA else region
            )
            .exceptionally {
                it.printStackTrace()
                return@exceptionally null
            }

        return selectedQueueMatch
    }
}
