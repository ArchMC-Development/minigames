package mc.arch.minigames.duelsmodern

import gg.tropic.practice.minigame.MiniGameMode

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
    }
}
