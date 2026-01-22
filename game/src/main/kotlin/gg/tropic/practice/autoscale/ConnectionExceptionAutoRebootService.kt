package gg.tropic.practice.autoscale

import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import me.lucko.helper.Events
import me.lucko.helper.Schedulers
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerQuitEvent
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Service that tracks players getting kicked for connection exceptions
 * (typically from Polar anti-cheat) and triggers an automatic server
 * reboot when the threshold is exceeded.
 *
 * @author Subham
 * @since 1/22/26
 */
@Service
object ConnectionExceptionAutoRebootService
{
    @Inject
    lateinit var plugin: ExtendedScalaPlugin

    /**
     * Pattern to match connection exception kicks (from Polar anti-cheat).
     * The message shown in console is: "lost connection: An exception occurred in your connection"
     */
    private const val CONNECTION_EXCEPTION_PATTERN = "An exception occurred"

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

                // Check if the quit message indicates a connection exception
                // The server log shows: "PlayerName lost connection: An exception occurred in your connection"
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
        plugin.logger.severe("[AutoReboot] Threshold of $KICK_THRESHOLD connection exception kicks exceeded!")
        plugin.logger.severe("[AutoReboot] Scheduling server drain and reboot in 5 seconds...")

        // Broadcast warning to all players
        Bukkit.getOnlinePlayers().forEach { player ->
            player.sendMessage("${CC.RED}${CC.BOLD}[SERVER] ${CC.RED}Server is experiencing connection issues and will reboot in 5 seconds.")
        }

        // Schedule the reboot after 5 seconds
        Schedulers.sync()
            .runLater({
                plugin.logger.severe("[AutoReboot] Initiating server shutdown due to connection exception threshold...")

                // Kick all players with a friendly message
                Bukkit.getOnlinePlayers().forEach { player ->
                    player.kickPlayer("${CC.RED}Server is rebooting due to connection issues. Please reconnect shortly.")
                }

                // Shutdown the server
                Bukkit.shutdown()
            }, 5, TimeUnit.SECONDS)
    }
}
