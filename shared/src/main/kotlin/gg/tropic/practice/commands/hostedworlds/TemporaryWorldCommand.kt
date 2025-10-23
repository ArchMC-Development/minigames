package gg.tropic.practice.commands.hostedworlds

import com.cryptomorin.xseries.XMaterial
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.tropic.practice.isMiniGameServer
import gg.tropic.practice.persistence.RedisShared
import gg.tropic.practice.ugc.HostedWorldRPC
import gg.tropic.practice.ugc.WorldInstanceProviderType
import gg.tropic.practice.ugc.generation.visits.VisitWorldRequest
import gg.tropic.practice.ugc.generation.visits.VisitWorldStatus
import gg.tropic.practice.ugc.temporaryworlds.TemporaryWorldVisitConfiguration
import mc.arch.minigames.parties.service.NetworkPartyService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author Subham
 * @since 7/20/25
 */
@AutoRegister
@CommandPermission("practice.command.temporaryworld")
object TemporaryWorldCommand : ScalaCommand()
{
    @CommandAlias("temporaryplot|temporaryworld")
    fun onTemporaryWorld(player: ScalaPlayer)
    {
        if (isMiniGameServer())
        {
            throw ConditionFailedException(
                "You cannot run this command in a game server!"
            )
        }

        val party = NetworkPartyService.findParty(player.uniqueId)
        val visitingPlayers = party?.includedMembers() ?: listOf(player.uniqueId)

        object : PaginatedMenu()
        {
            override fun getPrePaginatedTitle(player: Player) = "Select a floor type"

            override fun getAllPagesButtons(player: Player) = listOf(
                XMaterial.BIRCH_WOOD,
                XMaterial.OAK_WOOD,
                XMaterial.STONE,
                XMaterial.STONE_BRICKS
            ).map {
                ItemBuilder
                    .of(it)
                    .name("${CC.B_GREEN}SELECT")
                    .toButton { _, _ ->
                        Button.playNeutral(player)

                        player.closeInventory()
                        player.sendMessage("${CC.GRAY}Creating a temporary plot...")

                        HostedWorldRPC.visitWorldRPCService
                            .call(
                                VisitWorldRequest(
                                    visitingPlayers = visitingPlayers.toSet(),
                                    worldGlobalId = UUID.randomUUID(),
                                    configuration = TemporaryWorldVisitConfiguration(blockType = it.name),
                                    ownerPlayerId = player.uniqueId,
                                    providerType = WorldInstanceProviderType.TEMPORARY_WORLD
                                )
                            )
                            .thenAccept { response ->
                                if (response.status == VisitWorldStatus.SUCCESS_REDIRECT)
                                {
                                    player.sendMessage("${CC.GREEN}You are being sent to your temporary plot...")
                                    RedisShared.redirect(visitingPlayers, response.redirectToInstance!!)
                                    return@thenAccept
                                }

                                player.sendMessage("${CC.RED}We weren't able to create a temporary plot for you. (${response.status})")
                            }
                    }
            }.withIndex()
                .associate { it.index to it.value }
        }.openMenu(player)
    }
}
