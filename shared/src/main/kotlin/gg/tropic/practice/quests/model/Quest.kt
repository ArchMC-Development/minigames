package gg.tropic.practice.quests.model

import com.cryptomorin.xseries.XMaterial
import com.cryptomorin.xseries.XSound
import gg.scala.commons.ScalaCommons
import gg.tropic.practice.namespace
import gg.tropic.practice.quests.model.tracker.QuestTracker
import gg.tropic.practice.quests.model.tracker.QuestTrackerState
import gg.tropic.practice.statistics.StatisticLifetime
import me.lucko.helper.Helper
import me.lucko.helper.Schedulers
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.math.Numbers
import net.evilblock.cubed.util.text.TextSplitter
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import java.util.UUID
import java.util.concurrent.CompletableFuture
import kotlin.math.min

/**
 * @author Subham
 * @since 7/8/25
 */
data class Quest(
    val id: String,
    var name: String,
    var active: Boolean? = false,
    var lifetime: StatisticLifetime = StatisticLifetime.Daily,
    var description: String = "Win 25 games!",
    val rewards: MutableList<QuestReward> = mutableListOf(
        QuestReward()
    ),
    val requirements: MutableList<QuestRequirement> = mutableListOf(
        QuestRequirement()
    )
)
{
    fun toCurrentRedisNamespace() = "${namespace()}:quests:$id:${lifetime.getKeyID()}"
    fun toPlayerTrackerOf(player: UUID) = ScalaCommons
        .bundle().globals().redis().sync()
        .hget(
            toCurrentRedisNamespace(),
            player.toString()
        )
        ?.let {
            Serializers.gson.fromJson(it, QuestTracker::class.java)
        }
        ?: QuestTracker(questID = id, state = QuestTrackerState.INACTIVE)

    fun meetsAllRequirements(player: UUID) = requirements
        .fold(CompletableFuture.completedFuture(true)) { acc, requirement ->
            acc.thenCombine(requirement.meets(player)) { a, b -> a && b }
        }

    fun rewardAll(player: UUID) = CompletableFuture.allOf(
        *rewards
            .map { reward -> reward.reward(player) }
            .toTypedArray()
    )

    fun toPlayerState(player: UUID) = toPlayerTrackerOf(player).state
    fun updateState(player: UUID, newState: QuestTrackerState)
    {
        val tracker = toPlayerTrackerOf(player)
        tracker.state = newState

        ScalaCommons
            .bundle().globals().redis().sync()
            .apply {
                expireat(
                    toCurrentRedisNamespace(),
                    lifetime.getTimeAtReset()
                )
                hset(
                    toCurrentRedisNamespace(),
                    player.toString(),
                    Serializers.gson.toJson(tracker)
                )
            }
    }

    fun toInteractiveButton(player: Player, menu: Menu) = toPlayerState(player.uniqueId)
        .let { state ->
            ItemBuilder
                .of(XMaterial.PAPER)
                .name("${CC.B_YELLOW}${lifetime.name} Quest: $name")
                .setLore(
                    TextSplitter.split(
                        text = description,
                        linePrefix = "${CC.GRAY}",
                        wordSuffix = " "
                    )
                )
                .addToLore(
                    "",
                    "${CC.YELLOW}Requirements:"
                )
                .apply {
                    requirements.forEach { requirement ->
                        addToLore("${CC.GRAY}${requirement.description} ${CC.D_GRAY}(${
                            Numbers.format(min(
                                requirement.progress(player.uniqueId).join() ?: 0,
                                requirement.requirement
                            ))
                        }/${
                            Numbers.format(requirement.requirement)
                        })")
                    }
                }
                .addToLore(
                    "",
                    "${CC.YELLOW}Rewards:"
                )
                .apply {
                    if (rewards.isEmpty())
                    {
                        addToLore("${CC.RED}None! Check back later.")
                        return@apply
                    }

                    rewards.forEach { reward ->
                        addToLore(reward.toFancy())
                    }
                }
                .addToLore(
                    "",
                    "${CC.ID_GRAY}$lifetime quests can only be",
                    "${CC.ID_GRAY}completed once${
                        if (lifetime == StatisticLifetime.Weekly) " a week" else " a day"
                    }.",
                    "",
                )
                .apply {
                    addToLore(
                        when (state)
                        {
                            QuestTrackerState.INACTIVE -> "${CC.GREEN}Click to start this quest!"
                            QuestTrackerState.ACTIVE -> "${CC.GOLD}You are working on this quest!"
                            else -> "${CC.GOLD}You claimed quest rewards!"
                        }
                    )
                }
                .toButton { _, _ ->
                    if (player.hasMetadata("is-quest-processing"))
                    {
                        return@toButton
                    }

                    Button.playNeutral(player)
                    player.setMetadata(
                        "is-quest-processing",
                        FixedMetadataValue(Helper.hostPlugin(), true)
                    )

                    Schedulers
                        .async()
                        .run {
                            when (state)
                            {
                                QuestTrackerState.INACTIVE ->
                                {
                                    player.playSound(player.location, XSound.ENTITY_PLAYER_LEVELUP.parseSound(), 1.0f, 1.0f)
                                    player.sendMessage("${CC.GREEN}You activated the ${CC.GOLD}${lifetime.name} Quest: $name${CC.GREEN}!")
                                    updateState(player.uniqueId, QuestTrackerState.ACTIVE)
                                    menu.openMenu(player)
                                }

                                QuestTrackerState.ACTIVE ->
                                {
                                    player.sendMessage("${CC.RED}You are actively working on this quest! Check back later when you meet all requirements.")
                                }

                                else ->
                                {
                                    player.sendMessage("${CC.RED}You completed this quest! Check back ${if (lifetime == StatisticLifetime.Daily) "tomorrow" else "next week"}.")
                                }
                            }

                            player.removeMetadata(
                                "is-quest-processing",
                                Helper.hostPlugin()
                            )
                        }
                        .exceptionallyAsync { exception ->
                            exception.printStackTrace()

                            player.sendMessage("${CC.RED}We weren't able to process your quest interaction. Try again later.")
                            player.removeMetadata(
                                "is-quest-processing",
                                Helper.hostPlugin()
                            )
                            return@exceptionallyAsync null
                        }
                }
        }
}
