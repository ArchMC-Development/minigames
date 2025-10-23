package gg.tropic.practice.minigame

import gg.tropic.practice.player.LobbyPlayer
import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 6/27/25
 */
interface MinigameLobbyScoreboardProvider
{
    fun provideTitle(): String
    fun provideIdleLines(player: Player, lobbyProfile: LobbyPlayer): List<String>
}
