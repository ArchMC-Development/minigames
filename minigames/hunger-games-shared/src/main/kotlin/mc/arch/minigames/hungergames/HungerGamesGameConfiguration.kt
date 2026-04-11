package mc.arch.minigames.hungergames

import gg.tropic.practice.kit.feature.GameLifecycle
import gg.tropic.practice.minigame.MiniGameConfiguration

/**
 * @author ArchMC
 */
class HungerGamesGameConfiguration(
    val mode: HungerGamesMode,
    override val lifecycleType: GameLifecycle = GameLifecycle.SoulBound,
    override val orchestratorID: String = "hungergames",
    override val maximumPlayers: Int = mode.maxPlayers(),
    override val shouldBeAbleToReconnect: Boolean = false,
    override val reconnectThreshold: Long = 0L,
    override val maximumPlayersPerTeam: Int = mode.teamSize,
    override val gameDescription: String = "Survival Games ${mode.displayName}"
) : MiniGameConfiguration
{
    override val minimumPlayersRequiredToEnterStarting = mode.maxPlayers()
    override val minimumPlayersRequiredToFastForward = mode.maxPlayers()
    override val startGameCountDown = 30

    override fun getAbstractType() = HungerGamesGameConfiguration::class.java
}
