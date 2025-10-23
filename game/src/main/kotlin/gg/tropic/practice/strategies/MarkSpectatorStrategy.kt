package gg.tropic.practice.strategies

import com.cryptomorin.xseries.XMaterial
import gg.scala.basics.plugin.profile.BasicsProfileService
import gg.scala.commons.agnostic.sync.ServerSync
import gg.scala.commons.playerstatus.canVirtuallySee
import gg.scala.commons.playerstatus.isVirtuallyInvisibleToSomeExtent
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.redirection.impl.VelocityRedirectSystem
import gg.scala.lemon.util.QuickAccess
import gg.tropic.practice.expectation.ExpectationService.plugin
import gg.tropic.practice.expectation.ExpectationService.returnToSpawnItem
import gg.tropic.practice.expectation.ExpectationService.spectateItem
import gg.tropic.practice.games.GameService
import gg.tropic.practice.extensions.resetAttributes
import gg.tropic.practice.games.GameState
import gg.tropic.practice.minigame.MiniGameQueueConfiguration
import gg.tropic.practice.queue.QueueCommunications
import gg.tropic.practice.queue.QueueIDParser
import gg.tropic.practice.settings.isASilentSpectator
import gg.tropic.practice.ugc.toHostedWorld
import me.lucko.helper.Events
import me.lucko.helper.Helper
import me.lucko.helper.Schedulers
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.visibility.VisibilityHandler
import net.md_5.bungee.api.chat.ClickEvent
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.metadata.FixedMetadataValue

/**
 * @author Subham
 * @since 7/26/25
 */
@Service
object MarkSpectatorStrategy
{
    val playAgainItem = ItemBuilder
        .of(XMaterial.PAPER)
        .name("${CC.GREEN}Play Again ${CC.GRAY}(Right-Click)")
        .build()

    @Configure
    fun configure()
    {
        Events
            .subscribe(PlayerInteractEvent::class.java)
            .filter {
                it.hasItem() &&
                    (it.action == Action.RIGHT_CLICK_BLOCK || it.action == Action.RIGHT_CLICK_AIR) &&
                    it.item.isSimilar(playAgainItem)
            }
            .handler {
                playAgain(it.player)
            }
    }

    fun sendWinPlayAgain(player: Player)
    {
        FancyMessage()
            .withMessage(
                "${CC.GREEN}You won! ${CC.YELLOW}Want to play again? "
            )
            .withMessage("${CC.AQUA}Click here")
            .andHoverOf("${CC.AQUA}Click to play again!")
            .andCommandOf(
                ClickEvent.Action.RUN_COMMAND,
                "/playagain"
            )
            .sendToPlayer(player)
    }

    fun playAgain(player: Player)
    {
        val game = GameService
            .byPlayerOrSpectator(player.uniqueId)
            ?: return run {
                player.sendMessage("${CC.RED}You cannot use this right now!")
            }

        if (!(GameService.isSpectating(player) || game.state(GameState.Completed)))
        {
            player.sendMessage("${CC.RED}You cannot use this right now!")
            return
        }

        if (player.hasMetadata("play-again-confirmed"))
        {
            player.sendMessage("${CC.RED}We are trying to find a game for you! Please wait...")
            return
        }

        if (game.miniGameLifecycle != null && game.expectationModel.queueType != null)
        {
            Button.playNeutral(player)

            player.sendMessage("${CC.GREEN}Playing again! ${CC.GRAY}We are trying to find a game for you to join...")
            player.setMetadata("play-again-confirmed", FixedMetadataValue(Helper.hostPlugin(), true))

            val bracket = game.expectationModel.matchmakingMetadataAPIV2?.bracket
            QueueCommunications.joinQueue(
                kit = game.kit,
                queueType = game.expectationModel.queueType!!,
                teamSize = QueueIDParser.parseDetailed(game.expectationModel.queueId!!).teamSize,
                player = player,
                miniGameQueueConfiguration = MiniGameQueueConfiguration(
                    bracket = if (bracket == "") null else bracket,
                    excludeMiniInstance = ServerSync.local.id,
                    requiredMapID = null
                )
            )
        }
    }

    fun markSpectator(player: Player, world: World = player.world,
                      shouldAnnounce: Boolean = true,
                      shouldAddSpectatorItem: Boolean = false)
    {
        GameService.spectatorPlayers += player.uniqueId

        NametagHandler.reloadPlayer(player)
        VisibilityHandler.update(player)

        player.resetAttributes(editFlightAttributes = false)

        player.allowFlight = true
        player.isFlying = true

        val basicsProfile = BasicsProfileService.find(player)
        if (basicsProfile != null && shouldAnnounce)
        {
            if (player.isASilentSpectator() || player.isVirtuallyInvisibleToSomeExtent())
            {
                world.players
                    .filter(Player::isASilentSpectator)
                    .filter { other ->
                        other.canVirtuallySee(player)
                    }
                    .forEach { other ->
                        other.sendMessage(
                            "${CC.GRAY}(Silent) ${CC.GREEN}${
                                QuickAccess.coloredName(player)
                            }${CC.YELLOW} is now spectating."
                        )
                    }
            } else
            {
                world.players.forEach {
                    it.sendMessage(
                        "${CC.GREEN}${
                            QuickAccess.coloredName(player)
                        }${CC.YELLOW} is now spectating."
                    )
                }
            }
        }

        val activeGame = GameService.byPlayerOrSpectator(player.uniqueId)
        if (activeGame != null)
        {
            FancyMessage()
                .withMessage(
                    "${CC.GREEN}You are spectating! ${CC.YELLOW}Want to play again? "
                )
                .withMessage("${CC.AQUA}Click here")
                .andHoverOf("${CC.AQUA}Click to play again!")
                .andCommandOf(
                    ClickEvent.Action.RUN_COMMAND,
                    "/playagain"
                )
                .sendToPlayer(player)
        } else
        {
            player.sendMessage("${CC.GREEN}You are now spectating!")
        }

        Schedulers
            .sync()
            .runLater({
                if (shouldAddSpectatorItem)
                {
                    player.inventory.setItem(0, spectateItem)
                    if (activeGame != null)
                    {
                        player.inventory.setItem(7, playAgainItem)
                    }
                }

                player.inventory.setItem(8, returnToSpawnItem)
                player.updateInventory()
            }, 5L)
    }
}
