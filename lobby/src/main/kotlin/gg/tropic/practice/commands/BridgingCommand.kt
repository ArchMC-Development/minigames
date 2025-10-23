package gg.tropic.practice.commands

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.Conditions
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.tropic.practice.persistence.RedisShared
import gg.tropic.practice.ugc.HostedWorldRPC
import gg.tropic.practice.ugc.WorldInstanceProviderType
import gg.tropic.practice.ugc.generation.visits.VisitWorldRequest
import gg.tropic.practice.ugc.generation.visits.VisitWorldStatus
import mc.arch.minigames.microgames.bridging.api.BridgingMicrogameVisitConfiguration
import mc.arch.minigames.microgames.bridging.api.BridgingPracticeConfiguration
import mc.arch.minigames.microgames.bridging.api.profile.BridgingProfileOrchestrator
import net.evilblock.cubed.util.CC
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * @author Subham
 * @since 7/23/25
 */
@AutoRegister
object BridgingCommand : ScalaCommand()
{
    @CommandAlias("bridging|bridgepractice|bridgingpractice")
    fun onBridging(
        @Conditions("cooldown:duration=5,unit=SECONDS")
        player: ScalaPlayer
    ): CompletableFuture<*>
    {
        player.sendMessage("${CC.GRAY}Loading your bridging plot...")
        return HostedWorldRPC.visitWorldRPCService
            .call(
                VisitWorldRequest(
                    visitingPlayers = setOfNotNull(player.uniqueId),
                    worldGlobalId = UUID.randomUUID(),
                    configuration = BridgingMicrogameVisitConfiguration(
                        initialWorldConfiguration = BridgingProfileOrchestrator
                            .find(player.bukkit())
                            ?.configuration
                            ?: BridgingPracticeConfiguration()
                    ),
                    ownerPlayerId = player.uniqueId,
                    providerType = WorldInstanceProviderType.BRIDGING_PRACTICE
                )
            )
            .thenAccept { response ->
                if (response.status == VisitWorldStatus.SUCCESS_REDIRECT)
                {
                    player.sendMessage("${CC.GREEN}You are being sent to your bridging plot!")
                    RedisShared.redirect(listOf(player.uniqueId), response.redirectToInstance!!)
                    return@thenAccept
                }

                player.sendMessage("${CC.RED}We weren't able to create a bridging plot for you. (${response.status})")
            }
            .exceptionally { throwable ->
                throwable.printStackTrace()
                player.sendMessage("${CC.RED}We weren't able to create a bridging plot for you right now. Try again later!")
                return@exceptionally null
            }
    }
}
