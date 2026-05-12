package mc.arch.minigames.duelsmodern

import gg.tropic.practice.minigame.MiniGameMode
import gg.tropic.practice.provider.MiniProviderVersion

/**
 * @author ArchMC
 */
enum class DuelsModernMode(
    val displayName: String
) : MiniGameMode
{
    DUELS("Duels")
    {
        override val teamSize = 1
        override val teamCount = 2

        override fun maxPlayers() = 2
    };

    override val providerVersion: MiniProviderVersion = MiniProviderVersion.MODERN
}
