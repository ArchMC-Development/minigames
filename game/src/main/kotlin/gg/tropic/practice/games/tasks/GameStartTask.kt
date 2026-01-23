package gg.tropic.practice.games.tasks

import com.cryptomorin.xseries.XSound
import gg.tropic.practice.games.GameImpl
import gg.tropic.practice.games.GameService.plugin
import gg.tropic.practice.games.GameState
import gg.tropic.practice.games.event.GameStartEvent
import gg.tropic.practice.games.resetPlayerForFight
import gg.tropic.practice.kit.feature.FeatureFlag
import gg.tropic.practice.profile.PracticeProfileService
import gg.tropic.practice.queue.QueueType
import gg.tropic.practice.statistics.TrackedKitStatistic
import gg.tropic.practice.statistics.statisticIdFrom
import me.lucko.helper.scheduler.Task
import me.lucko.helper.terminable.composite.CompositeTerminable
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.ServerVersion
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.math.Numbers
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.TitlePart
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.metadata.FixedMetadataValue

/**
 * @author GrowlyX
 * @since 8/5/2022
 */
class GameStartTask(
    private val game: GameImpl
) : Runnable
{
    lateinit var task: Task

    override fun run()
    {
        if (this.game.currentGameStartCountdown >= 5)
        {
            this.game.state = GameState.Starting

            this.game.toBukkitPlayers()
                .filterNotNull()
                .forEach { player ->
                    // pre-generate
                    game.playerResourcesOf(player)

                    game.resetPlayerForFight(player)
                    game.enterLoadoutSelection(player)

                    player.getMetadata("life").firstOrNull()
                        ?.value()
                        ?.apply {
                            val terminable = this as CompositeTerminable
                            terminable.closeAndReportException()

                            player.setMetadata(
                                "life",
                                FixedMetadataValue(plugin, CompositeTerminable.create())
                            )
                        }

                    val profile = game.flagMetaData(
                        FeatureFlag.KnockbackProfile,
                        "profile"
                    )

                    if (profile != null && ServerVersion.getVersion().isOlderThan(ServerVersion.v1_9))
                    {
                        val customProfile = Bukkit.custom()
                            .knockbackManager.profileManager
                            .getProfile("default", profile)

                        customProfile?.setKnockback(player)
                    }
                }

            val teamVersus = this.game.teams
                .reversed()
                .map { it.players.size }
                .joinToString("v")

            val gameType = when (true)
            {
                (game.expectationModel.queueType == QueueType.Robot) -> "Bot Fight"
                (game.expectationModel.queueType != null) -> game.expectationModel.queueType!!.name
                (game.expectationModel.queueId == "tournament") -> "Tournament"
                (game.expectationModel.queueId == "party") -> "Party"
                else -> "Private"
            }

            val components = mutableListOf(
                "",
                "${CC.PRI}$gameType $teamVersus ${game.kit.displayName}:",
                "${CC.PRI}${Constants.THIN_VERTICAL_LINE}${CC.GRAY} Players: ${CC.WHITE}${
                    this.game.teams.first().players
                        .joinToString(", ") {
                            game.usernameOf(it)
                        }
                } and ${
                    if (this.game.robot())
                    {
                        this.game.robotInstance
                            .joinToString("${CC.WHITE}, ") { robot ->
                                robot.name()
                            }
                    } else
                    {
                        this.game.teams.last().players
                            .joinToString(", ") {
                                game.usernameOf(it)
                            }
                    }
                }"
            )

            if (game.expectationModel.queueType == QueueType.Ranked)
            {
                components += "${CC.PRI}${Constants.THIN_VERTICAL_LINE}${CC.GRAY} ELOs:"

                this.game.teams
                    .flatMap { it.toBukkitPlayers() }
                    .filterNotNull()
                    .forEach {
                        val statistic = PracticeProfileService.find(it)
                            ?.getCachedStatisticValue(
                                statisticIdFrom(TrackedKitStatistic.ELO) {
                                    kit(game.kit)
                                    ranked()
                                }
                            )
                            ?: return@forEach

                        components += "  ${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.WHITE}${it.name}: ${CC.GREEN}${statistic.score.toLong()}${
                            if (statistic.value != -1L) "${CC.GRAY}(#${
                                Numbers.format(statistic.value + 1)
                            })" else ""
                        }"
                    }
            }

            components += listOf(
                "${CC.PRI}${Constants.THIN_VERTICAL_LINE} ${CC.GRAY}Map: ${CC.WHITE}${game.map.displayName}",
                ""
            )

            this.game.sendMessage(
                *components.toTypedArray()
            )
        }

        when (this.game.currentGameStartCountdown)
        {
            5, 4, 3, 2, 1 ->
            {
                this.game.audiences {
                    it.sendTitlePart(
                        TitlePart.TITLE,
                        Component
                            .text(this.game.currentGameStartCountdown)
                            .decorate(TextDecoration.BOLD)
                            .color(NamedTextColor.GREEN)
                    )

                    it.sendTitlePart(
                        TitlePart.SUBTITLE,
                        Component.text(" ")
                    )
                }

                this.game.sendMessage(
                    "${CC.SEC}The game will start in ${CC.WHITE}${this.game.currentGameStartCountdown}${CC.SEC} second${
                        if (this.game.currentGameStartCountdown == 1) "" else "s"
                    }!"
                )
                this.game.playSound(XSound.BLOCK_NOTE_BLOCK_HAT.parseSound()!!)
            }
        }

        if (this.game.currentGameStartCountdown <= 0)
        {
            val event = GameStartEvent(this.game)
            Bukkit.getPluginManager().callEvent(event)

            if (event.isCancelled)
            {
                game.state = GameState.Completed
                game.closeAndCleanup()
                return
            }

            this.game.startTimestamp = System.currentTimeMillis()

            this.game.completeLoadoutSelection()
            this.game.sendMessage("${CC.GREEN}The game has started, good luck!")

            if (
                game.expectationModel.queueType == QueueType.Ranked ||
                game.expectationModel.queueId == "tournament"
            )
            {
                this.game.sendMessage(
                    " ",
                    "${CC.BD_RED}WARNING: ${CC.RED}Double Clicking is a punishable offence in all ranked matches. Adjusting your debounce time to 10ms or using a DC-prevention tool is highly recommended if you are unable to avoid double clicking.",
                    " "
                )

                /*if (Bukkit.getPluginManager().isPluginEnabled("anticheat"))
                {
                    fun Player.runAutoBanFor(reason: String)
                    {
                        val profile = uniqueId.offlineProfile
                        if (profile.hasActiveRankedBan())
                        {
                            return
                        }

                        profile.applyRankedBan(Duration.parse("7d"))
                        profile.deliverRankedBanMessage(player)
                        profile.saveAndPropagate()

                        Bukkit.dispatchCommand(
                            Bukkit.getConsoleSender(),
                            "terminatematch $name Anticheat Ban ($reason)"
                        )
                    }

                    game.toBukkitPlayers()
                        .filterNotNull()
                        .forEach { player ->
                            AnticheatFeature
                                .subscribeToSixtySecondSampleOf(
                                    player = player,
                                    check = AnticheatCheck.DOUBLE_CLICK,
                                    evaluate = { sample ->
                                        val thresholds = PracticeConfigurationService.cached().dataSampleThresholds()

                                        // If the player typically gets 3 or more violations in a 10-second period,
                                        // the player must be banned
                                        if (sample.accumulatedMedianOf() > thresholds.doubleClick)
                                        {
                                            player.runAutoBanFor("SADC")
                                        }
                                    }
                                )
                                .bindWith(game)

                            AnticheatFeature
                                .subscribeToSixtySecondSampleOf(
                                    player = player,
                                    check = AnticheatCheck.AUTO_CLICKER,
                                    evaluate = { sample ->
                                        val thresholds = PracticeConfigurationService.cached().dataSampleThresholds()

                                        // If the player typically gets 5 or more violations in a 10-second period,
                                        // the player must be banned
                                        if (sample.accumulatedMedianOf() > thresholds.autoClick)
                                        {
                                            player.runAutoBanFor("SAAC")
                                        }
                                    }
                                )
                                .bindWith(game)
                        }
                }*/
            }

            this.game.playSound(XSound.ENTITY_FIREWORK_ROCKET_BLAST.parseSound()!!, pitch = 1.0f)
            this.game.audiences { it.clearTitle() }

            this.game.state = GameState.Playing
            this.task.closeAndReportException()
            return
        }

        this.game.currentGameStartCountdown--
    }
}
