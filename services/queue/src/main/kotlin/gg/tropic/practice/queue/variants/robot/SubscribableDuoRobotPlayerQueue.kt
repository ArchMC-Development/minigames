package gg.tropic.practice.queue.variants.robot

import gg.tropic.practice.Globals
import gg.tropic.practice.application.api.defaults.kit.ImmutableKit
import gg.tropic.practice.application.api.defaults.map.MapDataSync
import gg.tropic.practice.expectation.GameExpectation
import gg.tropic.practice.games.team.GameTeam
import gg.tropic.practice.games.team.TeamIdentifier
import gg.tropic.practice.persistence.RedisShared
import gg.tropic.practice.queue.GameQueueManager
import gg.tropic.practice.queue.InternalQueue
import gg.tropic.practice.queue.QueueEntry
import gg.tropic.practice.queue.QueueType
import gg.tropic.practice.queue.SubscribablePlayerQueue
import gg.tropic.practice.queue.queueId
import gg.tropic.practice.region.Region
import java.util.UUID

/**
 * @author Subham
 * @since 6/15/25
 */
class SubscribableDuoRobotPlayerQueue(
    private val kit: ImmutableKit,
    private val teamSize: Int,
    override val id: String = queueId {
        kit(kit.id)
        queueType(QueueType.Robot)
        teamSize(teamSize)
    },
    override val internalQueue: InternalQueue<QueueEntry> = InternalQueue()
) : SubscribablePlayerQueue
{
    override fun onProcess(): List<QueueEntry>
    {
        val length = internalQueue.size()
        if (length < 2)
        {
            return emptyList()
        }

        val first: QueueEntry
        val second: QueueEntry

        val potentialGroups = playersInQueue()
            .map { entry ->
                val metadata = getBotMetadataOfPlayer(entry.data.leader)
                val otherEntriesMatchingEntry = playersInQueue()
                    .filter { otherEntry ->
                        val otherMetadata = getBotMetadataOfPlayer(otherEntry.data.leader)
                        entry != otherEntry &&
                            // ensure queueing same difficulty
                            otherMetadata?.botNamespace == metadata?.botNamespace
                    }

                otherEntriesMatchingEntry + entry
            }
            .filter {
                it.size >= 2
            }

        if (potentialGroups.isEmpty())
        {
            return emptyList()
        }

        val group = potentialGroups.random().map { it.data }

        // Expecting a symmetric list here, lets hope it doesn't break?
        first = group.firstOrNull()
            ?: return group

        second = group.lastOrNull()
            ?: return group

        val metadata = getBotMetadataOfPlayer(first.leader)
            ?: return group

        val robotPlayers = listOf(
            Globals.POSSIBLE_PLAYER_BOT_UNIQUE_IDS[0],
            Globals.POSSIBLE_PLAYER_BOT_UNIQUE_IDS[1]
        )

        // a bot is playing w/ the player lol
        val users = first.players + second.players + robotPlayers
        val map = MapDataSync
            .selectMapIfCompatible(
                kit, metadata.mapID
            )
            ?: return run {
                RedisShared.sendMessage(
                    // don't unnecessarily send message targeting the robot
                    first.players,
                    listOf(
                        "&cWe found no map compatible with the kit you are queueing for!"
                    )
                )

                group
            }

        val expectation = GameExpectation(
            identifier = UUID.randomUUID(),
            players = users.toMutableSet(),
            teams = setOf(
                GameTeam(
                    teamIdentifier = TeamIdentifier.A,
                    players = (first.players + second.players).toMutableSet()
                ),
                GameTeam(
                    teamIdentifier = TeamIdentifier.B,
                    players = robotPlayers.toMutableSet()
                ),
            ),
            kitId = kit.id,
            mapId = map.name,
            queueType = QueueType.Robot,
            queueId = id
        )

        val region = first.preferredQueueRegion
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
            .join()

        return group
    }
}
