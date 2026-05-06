package gg.tropic.practice.versioned

import gg.tropic.practice.games.event.GameStartEvent
import me.lucko.helper.Events
import me.lucko.helper.Schedulers
import net.evilblock.cubed.util.ServerVersion

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
                val knockback = Versioned.toProvider().getKnockbackProvider()

                Schedulers
                    .async()
                    .runLater({
                        event.game.allNonSpectators().forEach { player ->
                            knockback.applyProfile(player, "default", "minigames")
                        }
                    }, 10L)
            }
    }
}
