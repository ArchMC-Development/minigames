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
    override fun constructConfigurationForInitiatorEntry(entry: QueueEntry) =
        EventsMiniGameConfiguration(eventType = type, hostedBy = entry.leader)

    @Volatile
    private var lastCreationInitiatedAt = 0L

    override fun onProcess(): List<QueueEntry>
    {
        val targetEntry = playersInQueue().firstOrNull()?.data
            ?: return emptyList()

        val existing = GameManager.allGames().firstOrNull { it.queueId == id }
        if (existing != null && existing.state != GameState.Waiting && existing.state != GameState.Starting)
        {
            RedisShared.sendMessage(
                targetEntry.players,
                listOf("&cThis event is already in progress and cannot be joined right now.")
            )
            return listOf(targetEntry)
        }

        val willCreateNew = existing == null
        if (willCreateNew && System.currentTimeMillis() - lastCreationInitiatedAt < CREATION_GUARD_MS)
        {
            RedisShared.sendMessage(
                targetEntry.players,
                listOf("&cA ${type.name.lowercase()} event is already being created. Please try again in a moment.")
            )
            return listOf(targetEntry)
        }

        val result = super.onProcess()
        if (willCreateNew) lastCreationInitiatedAt = System.currentTimeMillis()
        return result
    }

    private companion object
    {
        const val CREATION_GUARD_MS = 5_000L
    }
}
