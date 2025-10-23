package mc.arch.lobby.main

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.tropic.game.extensions.cosmetics.CosmeticLocalConfig
import gg.tropic.practice.minigame.MinigameLobby

/**
 * @author Subham
 * @since 7/6/25
 */
@Service
object CustomizeMainLobbyService
{
    @Configure
    fun configure()
    {
        CosmeticLocalConfig.enableCosmeticResources = false
        MinigameLobby.customizeMainLobby()
    }
}
