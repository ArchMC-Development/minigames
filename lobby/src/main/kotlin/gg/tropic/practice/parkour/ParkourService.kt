package gg.tropic.practice.parkour

import com.cryptomorin.xseries.XMaterial
import com.cryptomorin.xseries.XSound
import gg.scala.cache.uuid.ScalaStoreUuidCache
import gg.scala.commons.ScalaCommons
import gg.scala.commons.spatial.toPosition
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.util.QuickAccess
import gg.tropic.practice.PracticeLobby
import gg.tropic.practice.configuration.PracticeConfigurationService
import gg.tropic.practice.leaderboards.LeaderboardEntry
import gg.tropic.practice.minigame.MinigameLobby
import gg.tropic.practice.namespace
import gg.tropic.practice.player.configureFlight
import gg.tropic.practice.profile.PracticeProfileService
import gg.tropic.practice.suffixWhenDev
import me.lucko.helper.Events
import me.lucko.helper.Schedulers
import net.evilblock.cubed.ScalaCommonsSpigot
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.EventUtils
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.cuboid.Cuboid
import net.evilblock.cubed.util.math.Numbers
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 3/18/2025
 */
@Service
object ParkourService
{
    @Inject
    lateinit var plugin: PracticeLobby

    var topTenLeaderboardPlayers = listOf<LeaderboardEntry>()
    var topTenLeaderboardEntries = listOf<String>()

    private val leaveParkourItem = ItemBuilder
        .of(XMaterial.RED_BED)
        .name("${CC.RED}Leave Parkour ${CC.GRAY}(Right Click)")
        .build()

    fun redisKey(): String
    {
        if (MinigameLobby.isMinigameLobby())
        {
            return "${namespace().suffixWhenDev()}:parkour:${
                PracticeConfigurationService.minigameType().provide().internalId
            }:leaderboards"
        }

        if (MinigameLobby.isMainLobby())
        {
            return "${namespace().suffixWhenDev()}:parkour:mainlobby:leaderboards"
        }

        return "${namespace().suffixWhenDev()}:parkour:leaderboards"
    }

    @Configure
    fun configure()
    {
        Events
            .subscribe(PlayerQuitEvent::class.java)
            .handler {
                it.player.endPlaySession()
            }
            .bindWith(plugin)

        Events
            .subscribe(PlayerInteractEvent::class.java)
            .filter {
                it.hasItem() && it.action == Action.RIGHT_CLICK_AIR && it.item.isSimilar(leaveParkourItem)
            }
            .handler {
                if (it.player.isPlayingParkour())
                {
                    leave(it.player, aborted = true)
                }
            }
            .bindWith(plugin)

        Schedulers
            .async()
            .runRepeating(Runnable {
                topTenLeaderboardPlayers = ScalaCommons.bundle().globals().redis()
                    .sync()
                    .zrangeWithScores(
                        redisKey(),
                        0, 9
                    )
                    .map { score ->
                        LeaderboardEntry(
                            uniqueId = UUID.fromString(score.value),
                            value = score.score.toLong()
                        )
                    }

                topTenLeaderboardEntries = topTenLeaderboardPlayers.mapIndexed { index, entry ->
                    "${CC.YELLOW}#${index + 1}. ${CC.WHITE}${
                        QuickAccess
                            .computeColoredName(
                                entry.uniqueId,
                                ScalaStoreUuidCache.username(entry.uniqueId) ?: "???"
                            )
                            .join()
                    } ${CC.GRAY}- ${CC.WHITE}${
                        entry.value.formatIntoTwoDecimal()
                    }s"
                }
            }, 0L, 100L)
            .bindWith(plugin)

        Events
            .subscribe(PlayerMoveEvent::class.java)
            .filter {
                EventUtils.hasPlayerMoved(it) && PracticeConfigurationService
                    .local()
                    .isParkourReady()
            }
            .handler {
                val configuration = PracticeConfigurationService
                    .local()
                    .parkourConfiguration

                if (it.player.isPlayingParkour())
                {
                    if (configuration.end!!.contains(it.player.location.toPosition()))
                    {
                        leave(it.player, aborted = false)
                    }
                } else
                {
                    if (configuration.start!!.contains(it.player.location.toPosition()))
                    {
                        play(it.player)
                    }
                }
            }
    }

    fun leave(player: Player, aborted: Boolean = true)
    {
        val playSession = player.extractPlaySession()
        val timeLasted = System.currentTimeMillis() - playSession.start
        player.endPlaySession()
        player.configureFlight()
        player.inventory.heldItemSlot = 0
        player.inventory.setItem(3, ItemStack(Material.AIR))
        player.updateInventory()

        player.teleport(
            PracticeConfigurationService.local().parkourConfiguration
                .priorStart!!
                .toLocation(player.world)
        )

        player.sendMessage("${CC.AQUA}Parkour:")
        player.sendMessage(
            "${CC.D_GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.GRAY}Time: ${CC.WHITE}${
                timeLasted.formatIntoTwoDecimal()
            }s"
        )

        if (aborted)
        {
            player.sendMessage("${CC.D_GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.I_RED}Aborted!")
        } else
        {
            player.sendMessage("${CC.D_GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.I_GREEN}Success!")

            CompletableFuture
                .supplyAsync { getScore(player) }
                .thenAccept { score ->
                    if (score == null || score.toLong() > timeLasted)
                    {
                        Schedulers.async().run {
                            ScalaCommons.bundle().globals().redis().sync()
                                .zadd(
                                    redisKey(),
                                    timeLasted.toDouble(),
                                    player.uniqueId.toString()
                                )

                            player.sendMessage("${CC.D_GRAY}${Constants.THIN_VERTICAL_LINE} ")
                            player.sendMessage("${CC.D_GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.B_GREEN}PERSONAL RECORD:")

                            if (score != null)
                            {
                                val beatBestBy = score.toLong() - timeLasted
                                player.sendMessage("${CC.D_GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.YELLOW}You beat your personal best by ${CC.B_YELLOW}${beatBestBy.formatIntoTwoDecimal()}s${CC.YELLOW}!")
                            }

                            player.sendMessage(
                                "${CC.D_GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.AQUA}Your position on the leaderboards is now: ${getLeaderboardPosition(player)}"
                            )
                        }
                    }
                }
        }
    }

    fun getLeaderboardPosition(player: Player) = ScalaCommons.bundle().globals().redis().sync()
        .zrank(
            redisKey(),
            player.uniqueId.toString()
        )
        ?.let {
            "#${Numbers.format(it + 1)}"
        }
        ?: "???"

    fun getScore(player: Player) = ScalaCommons.bundle().globals().redis().sync()
        .zscore(
            redisKey(),
            player.uniqueId.toString()
        )

    fun play(player: Player)
    {
        player.playSound(
            player.location,
            XSound.UI_BUTTON_CLICK.parseSound(),
            1.0f, 1.0f
        )

        player.gameMode = GameMode.SURVIVAL

        player.isFlying = false
        player.allowFlight = false

        player.inventory.setItem(3, leaveParkourItem)
        player.inventory.heldItemSlot = 3
        player.updateInventory()

        player.startPlaySession()

        Schedulers
            .async()
            .run {
                player.sendMessage("${CC.AQUA}Parkour:")
                player.sendMessage("${CC.D_GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.GRAY}Personal best: ${CC.WHITE}${
                    getScore(player)?.toLong()?.formatIntoTwoDecimal()?.let { "${it}s" } ?: "N/A"
                }")
                player.sendMessage("${CC.D_GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.GREEN}Use /spawn to leave parkour!")

                Schedulers.async().run {
                    player.sendMessage(
                        "${CC.D_GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.AQUA}Your position on the leaderboards is now: ${getLeaderboardPosition(player)}"
                    )
                }
            }
    }
}
