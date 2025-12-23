package gg.tropic.practice.privategames

import com.cryptomorin.xseries.XMaterial
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.redirection.impl.VelocityRedirectSystem
import gg.tropic.practice.expectation.ExpectationService
import gg.tropic.practice.games.GameState
import gg.tropic.practice.games.event.PlayerJoinGameEvent
import gg.tropic.practice.privategames.menu.PrivateGameSettingsMenu
import me.lucko.helper.Events
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Material
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.java.JavaPlugin
import java.time.Duration

/**
 * Service that adds hotbar items for private games in the waiting lobby.
 * Allows the party leader to configure game settings before the game starts.
 *
 * @author GrowlyX
 * @since 12/22/24
 */
@Service
object PrivateGamesHotbarService
{
    @Inject
    lateinit var plugin: ExtendedScalaPlugin

    private val settingsItem = ItemBuilder
        .of(XMaterial.COMPARATOR)
        .name("${CC.LIGHT_PURPLE}Game Settings ${CC.GRAY}(Right Click)")
        .addToLore(
            "${CC.GRAY}Configure private game",
            "${CC.GRAY}settings before start!"
        )
        .build()

    private val rateLimits = mutableMapOf<java.util.UUID, Long>()

    @Configure
    fun configure()
    {
        // Add settings item to private games on player join
        Events
            .subscribe(PlayerJoinGameEvent::class.java)
            .filter { it.game.expectationModel.isPrivateGame }
            .filter { it.game.state == GameState.Waiting || it.game.state == GameState.Starting }
            .handler { event ->
                if (event.game.expectationModel.players.firstOrNull() == event.player.uniqueId)
                {
                    event.player.inventory.setItem(4, settingsItem)
                    event.player.updateInventory()

                    event.player.sendMessage("${CC.PINK}This is a Private Game! ${CC.GRAY}Right-click the comparator to configure settings.")
                }
            }
            .bindWith(plugin)

        // Handle settings item click
        Events
            .subscribe(PlayerInteractEvent::class.java)
            .filter {
                it.hasItem() &&
                it.item.isSimilar(settingsItem) &&
                (it.action == Action.RIGHT_CLICK_AIR || it.action == Action.RIGHT_CLICK_BLOCK)
            }
            .handler { event ->
                val game = gg.tropic.practice.games.GameService.byPlayer(event.player)
                    ?: return@handler

                if (!game.expectationModel.isPrivateGame || !(game.state(GameState.Waiting) || game.state(GameState.Starting)))
                {
                    return@handler
                }

                // Rate limit check
                val lastClick = rateLimits[event.player.uniqueId] ?: 0L
                if (System.currentTimeMillis() - lastClick < 250L)
                {
                    return@handler
                }
                rateLimits[event.player.uniqueId] = System.currentTimeMillis()

                // Check if player is party leader (first in players list)
                if (game.expectationModel.players.firstOrNull() != event.player.uniqueId)
                {
                    event.player.sendMessage("${CC.RED}Only the party leader can modify game settings!")
                    return@handler
                }

                if (!(game.state == GameState.Waiting || game.state == GameState.Starting))
                {
                    event.player.sendMessage("${CC.RED}You can only modify settings before the game starts!")
                    return@handler
                }

                // Get game type from minigame lifecycle
                val gameType = game.miniGameLifecycle?.let {
                    game.flagMetaData(gg.tropic.practice.kit.feature.FeatureFlag.MiniGameType, "id")
                } ?: "default"

                PrivateGameSettingsMenu(game, gameType).openMenu(event.player)
            }
            .bindWith(plugin)
    }
}
