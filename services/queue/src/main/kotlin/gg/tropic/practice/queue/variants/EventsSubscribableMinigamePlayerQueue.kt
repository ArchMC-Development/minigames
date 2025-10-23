package gg.tropic.practice.queue.variants

import gg.tropic.practice.application.api.defaults.kit.ImmutableKit
import gg.tropic.practice.queue.AbstractSubscribableMinigamePlayerQueue
import gg.tropic.practice.queue.QueueEntry
import gg.tropic.practice.queue.QueueType
import mc.arch.minigame.bedwars.neo.BedWarsGameConfiguration
import mc.arch.minigame.bedwars.neo.BedWarsMode
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
    queueType = QueueType.Casual // No support for ranked yet
)
{
    override fun constructConfigurationForInitiatorEntry(entry: QueueEntry) =
        EventsMiniGameConfiguration(eventType = type, hostedBy = entry.leader)
}
