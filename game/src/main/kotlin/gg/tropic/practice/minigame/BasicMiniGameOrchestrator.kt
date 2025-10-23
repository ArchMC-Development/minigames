package gg.tropic.practice.minigame

import gg.tropic.practice.expectation.GameExpectation
import gg.tropic.practice.kit.Kit
import org.bukkit.World

/**
 * @author GrowlyX
 * @since 8/23/2024
 */
abstract class BasicMiniGameOrchestrator<T : MiniGameConfiguration> : MiniGameOrchestrator<T>
{
    abstract fun prepare(miniGame: AbstractMiniGameGameImpl<T>, configuration: T): MiniGameLifecycle<T>
    override fun construct(arenaWorld: World, kit: Kit, expectation: GameExpectation): AbstractMiniGameGameImpl<T>
    {
        return object : AbstractMiniGameGameImpl<T>(arenaWorld, this, expectation, kit)
        {
            override fun prepare(configuration: T) = this@BasicMiniGameOrchestrator.prepare(this, configuration)
                .apply {
                    bindWith(game)
                }
        }
    }
}
