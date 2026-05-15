package gg.tropic.practice.queue.variants

import gg.tropic.practice.application.api.defaults.kit.ImmutableKit
import gg.tropic.practice.games.GameState
import gg.tropic.practice.games.manager.GameManager
import gg.tropic.practice.persistence.RedisShared
import gg.tropic.practice.provider.MiniProviderVersion
import gg.tropic.practice.queue.AbstractSubscribableMinigamePlayerQueue
import gg.tropic.practice.queue.QueueEntry
import gg.tropic.practice.queue.QueueType
import mc.arch.minigames.microgames.events.EventType
import mc.arch.minigames.microgames.events.EventsMiniGameConfiguration

/**
 * @author Subham
 * @since 6/15/25
 */
class EventsSubscribableMinigamePlayerQueue(
    kit: ImmutableKit,
    private val type: EventType
) : AbstractSubscribableMinigamePlayerQueue(
    miniGameMode = type,
    kit = kit,
    selectNewestInstance = true,
    queueType = QueueType.Casual, // No support for ranked yet
    miniProvider = type.providerVersion
)
{
    override val createsFallbackGameOnJoinFailure: Boolean = false

    override fun constructConfigurationForInitiatorEntry(entry: QueueEntry) =
        EventsMiniGameConfiguration(eventType = type, hostedBy = entry.leader)

    @Volatile
    private var lastCreationInitiatedAt = 0L

    override fun onProcess(): List<QueueEntry>
    {
        val targetEntry = playersInQueue().firstOrNull()?.data
            ?: return emptyList()

        val existing = GameManager.allGames().firstOrNull { it.queueId == id }
        if (existing != null)
        {
            if (existing.state != GameState.Waiting && existing.state != GameState.Starting)
            {
                RedisShared.sendMessage(
                    targetEntry.players,
                    listOf("&cThis event is already in progress and cannot be joined right now.")
                )
                return listOf(targetEntry)
            }

            // Singleton invariant: a joinable event already exists. Don't let the
            // base class spawn a parallel one just because the host's instance has
            // been flagged failing — make the player wait instead.
            if (existing.server in AbstractSubscribableMinigamePlayerQueue.getFailingInstances())
            {
                RedisShared.sendMessage(
                    targetEntry.players,
                    listOf("&cThe ${type.name.lowercase()} event is unavailable. Please try again later.")
                )
                return listOf(targetEntry)
            }

            // Singleton invariant: when the event is at capacity, base.onProcess()
            // would filter the existing game out (it can't fit the joiner) and fall
            // through to spawning a parallel event. Reject instead.
            if (existing.players.size + targetEntry.players.size > type.maxPlayers())
            {
                RedisShared.sendMessage(
                    targetEntry.players,
                    listOf("&cThis ${type.name.lowercase()} event is full.")
                )
                return listOf(targetEntry)
            }

            return super.onProcess()
        }

        // No existing event yet. The just-created game can take several seconds to
        // show up in GameManager's cache; gate the create-new path so a second
        // joiner during that window doesn't spawn a parallel event.
        if (System.currentTimeMillis() - lastCreationInitiatedAt < CREATION_GUARD_MS)
        {
            RedisShared.sendMessage(
                targetEntry.players,
                listOf("&cA ${type.name.lowercase()} event is already being created. Please try again in a moment.")
            )
            return listOf(targetEntry)
        }

        lastCreationInitiatedAt = System.currentTimeMillis()
        return super.onProcess()
    }

    private companion object
    {
        const val CREATION_GUARD_MS = 30_000L
    }
}
