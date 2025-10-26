package mc.arch.lobby.main

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.tropic.game.extensions.cosmetics.CosmeticLocalConfig
import gg.tropic.game.extensions.music.Music
import gg.tropic.game.extensions.music.Playlist
import gg.tropic.practice.minigame.MinigameLobby
import me.lucko.helper.Events
import me.lucko.helper.Schedulers
import org.bukkit.event.player.PlayerJoinEvent

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
        MinigameLobby.customizeMainLobby()

        Events
            .subscribe(PlayerJoinEvent::class.java)
            .handler { event ->
                Schedulers
                    .sync()
                    .runLater({
                        Playlist(
                            musicDiscs = listOf(
                                Music.Strad,
                                Music.Cat,
                                Music.Wait,
                            )
                        ).play(event.player)
                    }, 20L)
            }
    }
}
