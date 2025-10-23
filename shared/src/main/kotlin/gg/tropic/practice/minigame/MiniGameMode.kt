package gg.tropic.practice.minigame

/**
 * @author Subham
 * @since 6/15/25
 */
interface MiniGameMode
{
    val teamSize: Int
    val teamCount: Int

    fun maxPlayers(): Int
}
