package gg.tropic.practice.quests

import com.cryptomorin.xseries.XSound
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.util.QuickAccess
import gg.tropic.practice.games.event.GameCompleteEvent
import gg.tropic.practice.persistence.RedisShared
import gg.tropic.practice.quests.model.tracker.QuestTrackerState
import me.lucko.helper.Events
import me.lucko.helper.Schedulers
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import org.bukkit.Bukkit
import org.bukkit.Sound

/**
 * @author Subham
 * @since 7/8/25
 */
@Service
object QuestTrackerService
{
    @Configure
    fun configure()
    {
        Events
            .subscribe(GameCompleteEvent::class.java)
            .filter { event -> event.game.miniGameLifecycle != null }
            .handler { event ->
                val players = event.game.expectationModel.players.toSet()
                Schedulers
                    .async()
                    .runLater({
                        val systemEnabledQuests = QuestsService
                            .getLimitedActiveMinigameQuests(event.game.minigameType()!!)

                        players.forEach { player ->
                            systemEnabledQuests
                                .filter { quest ->
                                    quest.toPlayerState(player) == QuestTrackerState.ACTIVE
                                }
                                .forEach { quest ->
                                    val meetsRequirements = quest.meetsAllRequirements(player).join()
                                    if (meetsRequirements)
                                    {
                                        quest.updateState(player, QuestTrackerState.CLAIMED)
                                        quest.rewardAll(player).join()

                                        QuickAccess.sendGlobalPlayerFancyMessage(
                                            fancyMessage = FancyMessage()
                                                .withMessage("${CC.GREEN}You completed the ${CC.GOLD}${quest.name} quest!${CC.GREEN}")
                                                .apply {
                                                    quest.rewards.forEach { reward ->
                                                        withMessage("\n" + reward.toFancy())
                                                    }
                                                },
                                            uuid = player
                                        )

                                        val bukkitPlayer = Bukkit.getPlayer(player)
                                            ?: return@forEach

                                        bukkitPlayer.playSound(
                                            bukkitPlayer.location,
                                            XSound.ENTITY_PLAYER_LEVELUP.parseSound(),
                                            1.0f, 1.0f
                                        )
                                    }
                                }
                        }
                    }, 10L)
            }
    }
}
