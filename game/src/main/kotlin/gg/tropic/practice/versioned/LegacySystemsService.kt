package gg.tropic.practice.versioned

import gg.tropic.practice.games.event.GameStartEvent
import me.lucko.helper.Events
import me.lucko.helper.Schedulers
import net.evilblock.cubed.util.ServerVersion
import org.bukkit.Bukkit

/**
 * @author Subham
 * @since 8/5/25
 */
class LegacySystemsService
{
    fun configure()
    {
        Events
            .subscribe(GameStartEvent::class.java)
            .filter { ServerVersion.getVersion().isOlderThan(ServerVersion.v1_9) }
            .handler { event ->
                val customProfile = Bukkit.custom()
                    .knockbackManager.profileManager
                    .getProfile("default", "minigames")
                    ?: return@handler

                Schedulers
                    .async()
                    .runLater({
                        event.game.allNonSpectators().forEach { player ->
                            customProfile.setKnockback(player)
                        }
                    }, 10L)
            }
    }
}
