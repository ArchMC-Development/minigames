package gg.tropic.practice.minigame.handler

import gg.scala.commons.agnostic.sync.ServerSync
import gg.tropic.practice.games.restart.RestartInstanceRequest
import gg.tropic.practice.games.restart.RestartInstanceResponse
import gg.tropic.practice.games.restart.RestartStatus
import io.sentry.Sentry
import io.sentry.SentryLevel
import mc.arch.commons.communications.rpc.RPCContext
import mc.arch.commons.communications.rpc.RPCHandler
import net.evilblock.cubed.reboot.ShutdownService
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import java.util.concurrent.TimeUnit

/**
 * Handles restart requests from the queue server when this instance
 * is failing RPC calls excessively.
 *
 * @author Subham
 * @since 1/22/26
 */
class RestartInstanceHandler : RPCHandler<RestartInstanceRequest, RestartInstanceResponse>
{
    override fun handle(
        request: RestartInstanceRequest,
        context: RPCContext<RestartInstanceResponse>
    )
    {
        // Only handle if this is the target server
        if (ServerSync.local.id != request.targetServer)
        {
            return
        }

        Sentry.addBreadcrumb(io.sentry.Breadcrumb().apply {
            category = "instance.restart"
            message = "Received restart request: ${request.reason}"
            level = SentryLevel.WARNING
            setData("delay_seconds", request.delaySeconds)
        })

        // Check if already draining/restarting
        if (ServerSync.getLocalGameServer().isDraining() || ShutdownService.isRebooting())
        {
            context.reply(
                RestartInstanceResponse(
                    status = RestartStatus.ALREADY_DRAINING,
                    message = "Server is already draining"
                )
            )
            return
        }

        Sentry.captureMessage("Instance restart triggered by queue server") { scope ->
            scope.level = SentryLevel.WARNING
            scope.setTag("alert_type", "forced_restart")
            scope.setExtra("reason", request.reason)
            scope.setExtra("delay_seconds", request.delaySeconds.toString())
        }

        // Broadcast warning to online players
        Bukkit.getOnlinePlayers().forEach { player ->
            player.sendMessage("${CC.RED}${CC.BOLD}[SERVER] ${CC.RED}This server will restart in ${request.delaySeconds} seconds due to connectivity issues.")
        }

        // Schedule the restart
        ShutdownService.initiateShutdown(request.delaySeconds)

        context.reply(
            RestartInstanceResponse(
                status = RestartStatus.SUCCESS,
                message = "Restart scheduled in ${request.delaySeconds} seconds"
            )
        )
    }
}
