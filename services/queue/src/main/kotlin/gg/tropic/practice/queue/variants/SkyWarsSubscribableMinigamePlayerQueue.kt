package gg.tropic.practice.queue.variants

import gg.tropic.practice.application.api.defaults.kit.ImmutableKit
import gg.tropic.practice.provider.MiniProviderVersion
import gg.tropic.practice.queue.AbstractSubscribableMinigamePlayerQueue
import gg.tropic.practice.queue.QueueEntry
import gg.tropic.practice.queue.QueueType
import mc.arch.minigame.bedwars.neo.BedWarsGameConfiguration
import mc.arch.minigame.bedwars.neo.BedWarsMode
import mc.arch.minigames.skywars.SkyWarsGameConfiguration
import mc.arch.minigames.skywars.SkyWarsMode

/**
 * @author Subham
 * @since 6/15/25
 */
class SkyWarsSubscribableMinigamePlayerQueue(
    kit: ImmutableKit,
    private val mode: SkyWarsMode,
) : AbstractSubscribableMinigamePlayerQueue(
    miniGameMode = mode,
    kit = kit,
    queueType = mode.queueType,
    miniProvider = mode.providerVersion
)
{
    override fun constructConfigurationForInitiatorEntry(entry: QueueEntry) =
        SkyWarsGameConfiguration(mode = mode)
}
