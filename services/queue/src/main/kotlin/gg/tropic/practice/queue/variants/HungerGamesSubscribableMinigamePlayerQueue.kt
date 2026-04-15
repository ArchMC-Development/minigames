package gg.tropic.practice.queue.variants

import gg.tropic.practice.application.api.defaults.kit.ImmutableKit
import gg.tropic.practice.queue.AbstractSubscribableMinigamePlayerQueue
import gg.tropic.practice.queue.QueueEntry
import gg.tropic.practice.queue.QueueType
import mc.arch.minigame.bedwars.neo.BedWarsGameConfiguration
import mc.arch.minigame.bedwars.neo.BedWarsMode
import mc.arch.minigame.miniwalls.MiniWallsGameConfiguration
import mc.arch.minigame.miniwalls.MiniWallsGameFormat
import mc.arch.minigame.miniwalls.MiniWallsMode
import mc.arch.minigames.hungergames.HungerGamesGameConfiguration
import mc.arch.minigames.hungergames.HungerGamesMode

/**
 * @author Subham
 * @since 6/15/25
 */
class HungerGamesSubscribableMinigamePlayerQueue(
    kit: ImmutableKit,
    private val mode: HungerGamesMode
) : AbstractSubscribableMinigamePlayerQueue(
    miniGameMode = mode,
    kit = kit,
    selectNewestInstance = true,
    queueType = QueueType.Casual // No support for ranked yet
)
{
    override fun constructConfigurationForInitiatorEntry(entry: QueueEntry) =
        HungerGamesGameConfiguration(mode = mode)
}
