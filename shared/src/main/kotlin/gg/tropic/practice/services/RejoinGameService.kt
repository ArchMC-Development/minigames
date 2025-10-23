package gg.tropic.practice.services

import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.agnostic.sync.ServerSync
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.tropic.practice.minigame.rejoin.toRejoinToken
import me.lucko.helper.Events
import me.lucko.helper.Schedulers
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.evilblock.cubed.util.time.TimeUtil
import net.md_5.bungee.api.chat.ClickEvent
import org.bukkit.event.player.PlayerJoinEvent

/**
 * @author Subham
 * @since 6/16/25
 */
@Service
object RejoinGameService
{
    @Inject
    lateinit var plugin: ExtendedScalaPlugin

    @Configure
    fun configure()
    {
        if (ServerSync.local.groups.contains("mipgame"))
        {
            return
        }

        Events
            .subscribe(PlayerJoinEvent::class.java)
            .handler { event ->
                Schedulers
                    .async()
                    .runLater({
                        val rejoinToken = event.player.uniqueId.toRejoinToken()
                            ?: return@runLater

                        if (event.player.isOnline && System.currentTimeMillis() < rejoinToken.expiration)
                        {
                            event.player.sendMessage("")
                            event.player.sendMessage("${CC.GREEN}You were previously in a ${CC.B_GREEN}${rejoinToken.gameDescription}${CC.GREEN} game.")
                            event.player.sendMessage("${CC.GRAY}Time left to rejoin: ${
                                TimeUtil.formatMillisIntoAbbreviatedString(rejoinToken.expiration - System.currentTimeMillis())
                            } to rejoin.")

                            FancyMessage()
                                .withMessage("${CC.GREEN}[Rejoin]")
                                .andHoverOf(
                                    "${CC.GRAY}By clicking this, you will",
                                    "${CC.GRAY}be sent to the server:",
                                    "${CC.WHITE}${rejoinToken.server}",
                                    "",
                                    "${CC.GREEN}Click to rejoin!"
                                )
                                .andCommandOf(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/minigames:rejoin"
                                )
                                .withMessage("${CC.RED} [Ignore]")
                                .andHoverOf(
                                    "${CC.GRAY}By clicking this, you will",
                                    "${CC.GRAY}choose to forfeit your",
                                    "${CC.WHITE}${rejoinToken.gameDescription} ${CC.GRAY}game.",
                                    "",
                                    "${CC.RED}Click to ignore!"
                                )
                                .andCommandOf(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/minigames:rejoin ignore"
                                )
                                .sendToPlayer(event.player)

                            event.player.sendMessage("")
                        }
                    }, 20L)
                    .bindWith(plugin)
            }
            .bindWith(plugin)
    }
}
