package gg.tropic.practice.queue.variants.robot

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
class SubscribableSoloRobotPlayerQueue(
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
        if (length < 1)
        {
            return emptyList()
        }

        val first = playersInQueue().first()
        val metadata = getBotMetadataOfPlayer(first.data.leader)

        if (metadata == null)
        {
            RedisShared.sendMessage(
                // don't unnecessarily send message targeting the robot
                first.data.players,
                listOf(
                    "&cYou did not select a kit to play properly!"
                )
            )

            return listOf(first.data)
        }

        // a bot is playing w/ the player lol
        val users = first.data.players + metadata.botInstances
        val map = MapDataSync
            .selectMapIfCompatible(
                kit, metadata.mapID
            )
            ?: return run {
                RedisShared.sendMessage(
                    // don't unnecessarily send message targeting the robot
                    first.data.players,
                    listOf(
                        "&cWe found no map compatible with the kit you are queueing for!"
                    )
                )

                listOf(first.data)
            }

        val expectation = GameExpectation(
            identifier = UUID.randomUUID(),
            players = users.toMutableSet(),
            teams = setOf(
                GameTeam(teamIdentifier = TeamIdentifier.A, players = first.data.players.toMutableSet()),
                GameTeam(teamIdentifier = TeamIdentifier.B, players = metadata.botInstances.toMutableSet()),
            ),
            kitId = kit.id,
            mapId = map.name,
            queueType = QueueType.Robot,
            queueId = id
        )

        val region = first.data.preferredQueueRegion
        GameQueueManager.prepareGameFor(
            map = map,
            expectation = expectation,
            // prefer NA servers if queuing globally
            region = if (region == Region.Both) Region.NA else region
        ).exceptionally {
            it.printStackTrace()
            return@exceptionally null
        }

        return listOf(first.data)
    }
}
