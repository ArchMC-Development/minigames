package gg.tropic.practice.cooldown

import com.cryptomorin.xseries.XMaterial
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.cooldown.CooldownHandler
import gg.scala.lemon.cooldown.CooldownHandler.notifyAndContinueNoBypass
import gg.scala.lemon.cooldown.type.PlayerStaticCooldown
import gg.tropic.practice.PracticeGame
import gg.tropic.practice.games.GameService
import gg.tropic.practice.kit.feature.FeatureFlag
import me.lucko.helper.Events
import me.lucko.helper.Schedulers
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.EnderPearl
import org.bukkit.entity.Player
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.player.PlayerInteractEvent
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.round
import kotlin.math.roundToInt

/**
 * @author GrowlyX
 * @since 11/21/2021
 */
@Service
object EnderPearlCooldown : PlayerStaticCooldown(
    "Enderpearl", 0L
)
{
    @Inject
    lateinit var plugin: PracticeGame

    override fun durationFor(t: Player): Long
    {
        val game = GameService.byPlayer(t)
            ?: return 0L

        val duration = game.kit
            .featureConfig(
                FeatureFlag.EnderPearlCooldown,
                "duration"
            )

        return TimeUnit.SECONDS.toMillis(duration.toLong())
    }

    @Configure
    fun configure()
    {
        this.notifyOnExpiration()
        this.whenExpired {
            it.sendMessage("${CC.GRAY}Your enderpearl cooldown has expired.");
        }
        this.scheduleLevelChangeAfterCooldownChange()
        this.blockUnwantedEnderpearls()
        this.launchEnderpearlCooldown()
    }

    private fun scheduleLevelChangeAfterCooldownChange()
    {
        Schedulers.async().runRepeating(Runnable {
            for (id in tasks.keys)
            {
                val player = Bukkit.getPlayer(id) ?: continue
                val time = fetchRemaining(player)
                if (time < 0) continue

                val game = GameService.byPlayer(player) ?: continue
                val duration = game.kit.featureConfig(
                    FeatureFlag.EnderPearlCooldown,
                    "duration"
                ).toFloat()

                val seconds = (time.toDouble() / 1000.0).roundToInt()

                player.level = max(seconds, 0)
                player.exp = (time.toFloat() / (duration * 1000.0f)).coerceIn(0.0f, 1.0f)
            }
        }, 0L, 1L)
    }

    private fun blockUnwantedEnderpearls()
    {
        Events.subscribe(PlayerInteractEvent::class.java)
            .filter {
                it.hasItem()
                    && it.action.name.contains("RIGHT")
                    && it.item.type == XMaterial.ENDER_PEARL.get()
            }
            .handler {
                val player = it.player
                val game = GameService.byPlayer(player) ?: return@handler

                if (!game.ensurePlaying() || GameService.isLightSpectating(player))
                {
                    it.isCancelled = true
                    player.updateInventory()
                    player.sendMessage("${CC.RED}You cannot throw enderpearls right now!")
                    return@handler
                }

                if (!notifyAndContinueNoBypass(this.javaClass, player, "throwing an enderpearl"))
                {
                    it.isCancelled = true
                    player.updateInventory()
                }
            }
            .bindWith(plugin)
    }

    private fun launchEnderpearlCooldown()
    {
        Events.subscribe(ProjectileLaunchEvent::class.java)
            .filter { it.entity is EnderPearl && it.entity.shooter is Player }
            .handler {
                val player = it.entity.shooter as Player
                if (!it.isCancelled)
                {
                    val game = GameService.byPlayer(player) ?: return@handler
                    if (game.flag(FeatureFlag.EnderPearlCooldown))
                    {
                        addOrOverride(player)
                    }
                }
            }
            .bindWith(plugin)

        CooldownHandler.register(this)
    }
}
