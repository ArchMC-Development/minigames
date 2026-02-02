package gg.tropic.practice.autoscale

import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import me.lucko.helper.Events
import me.lucko.helper.Schedulers
import net.evilblock.cubed.reboot.ScheduledServerRebootService
import net.evilblock.cubed.reboot.ShutdownService
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerQuitEvent
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Subham
 * @since 1/22/26
 */
@Service
object ConnectionExceptionAutoRebootService
{
    @Inject
    lateinit var plugin: ExtendedScalaPlugin

    private const val CONNECTION_EXCEPTION_PATTERN = "Please report this to server staff or to Polar directly"

    /**
     * Number of connection exception kicks before triggering auto-reboot.
     */
    private const val KICK_THRESHOLD = 3

    /**
     * Time window in milliseconds to track kicks.
     * Resets if no kicks happen within this window.
     */
    private const val TRACKING_WINDOW_MS = 60_000L // 1 minute

    private val connectionExceptionKickCount = AtomicInteger(0)
    private var lastKickTimestamp = 0L
    private val rebootScheduled = AtomicBoolean(false)

    @Configure
    fun configure()
    {
        Events
            .subscribe(PlayerQuitEvent::class.java)
            .handler { event ->
                val quitMessage = event.quitMessage ?: return@handler
                if (!quitMessage.contains(CONNECTION_EXCEPTION_PATTERN, ignoreCase = true))
                {
                    return@handler
                }

                handleConnectionExceptionKick(event.player.name)
            }
            .bindWith(plugin)

        plugin.logger.info("ConnectionExceptionAutoRebootService initialized with threshold of $KICK_THRESHOLD kicks")
    }

    private fun handleConnectionExceptionKick(playerName: String)
    {
        val currentTime = System.currentTimeMillis()

        // Reset counter if outside the tracking window
        if (currentTime - lastKickTimestamp > TRACKING_WINDOW_MS)
        {
            connectionExceptionKickCount.set(0)
        }

        lastKickTimestamp = currentTime
        val currentCount = connectionExceptionKickCount.incrementAndGet()

        plugin.logger.warning(
            "[AutoReboot] Connection exception kick detected for $playerName " +
            "($currentCount/$KICK_THRESHOLD within tracking window)"
        )

        if (currentCount >= KICK_THRESHOLD && rebootScheduled.compareAndSet(false, true))
        {
            scheduleAutoReboot()
        }
    }

    private fun scheduleAutoReboot()
    {
        ShutdownService.initiateShutdown(6)
    }
}
