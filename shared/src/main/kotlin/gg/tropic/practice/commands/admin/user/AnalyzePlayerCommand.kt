package gg.tropic.practice.commands.admin.user

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandCompletion
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Conditions
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.scala.commons.playerstatus.PlayerStatusTrackerService
import gg.scala.friends.friendship.Friendship
import gg.scala.friends.service.FriendsService
import gg.scala.lemon.handler.GrantHandler
import gg.scala.lemon.handler.PunishmentHandler
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.grant.Grant
import gg.scala.lemon.player.punishment.Punishment
import gg.scala.lemon.player.rank.Rank
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import gg.scala.lemon.util.QuickAccess
import gg.scala.staff.anticheat.AnticheatLog
import gg.scala.staff.anticheat.AnticheatProfileService
import gg.tropic.game.extensions.economy.Currency
import gg.tropic.game.extensions.economy.EconomyDataSync
import gg.tropic.game.extensions.economy.EconomyProfileService
import gg.tropic.game.extensions.guilds.Guild
import gg.tropic.game.extensions.guilds.GuildService
import gg.tropic.game.extensions.profile.CorePlayerProfile
import gg.tropic.game.extensions.profile.CorePlayerProfileService
import gg.tropic.practice.commands.menu.user.PlayerAnalysisMenu
import gg.tropic.practice.games.GameReference
import mc.arch.minigames.parties.model.Party
import mc.arch.minigames.parties.service.NetworkPartyService
import net.evilblock.cubed.util.CC
import java.util.concurrent.TimeUnit

/**
 * Class created on 7/11/2025

 * @author Max C.
 * @project arch-minigames
 * @website https://solo.to/redis
 */
@AutoRegister
object AnalyzePlayerCommand : ScalaCommand()
{
    @CommandAlias("analyzeplayer")
    @CommandCompletion("@players")
    @CommandPermission("practice.command.analyzeplayer")
    fun onAnalyzePlayer(@Conditions("cooldown:duration=10,unit=SECONDS") sender: ScalaPlayer, target: AsyncLemonPlayer) =
        target.validatePlayers(sender.bukkit(), false) { profile ->
            sender.sendMessage("${CC.GRAY}Loading player data...")

            val coloredName = QuickAccess.computeColoredName(profile.uniqueId, profile.name).join()

            val economyProfile =
                EconomyProfileService.find(profile.uniqueId) ?: EconomyProfileService.loadCopyOf(profile.uniqueId)
                    .join()
            val currencies = EconomyDataSync.cached().economies.values
                .associate { it.currency to (economyProfile?.balance(it.id) ?: 0L) }

            val guild = GuildService.guildByUser(profile.uniqueId).join()
            val status = PlayerStatusTrackerService.loadStatusOf(profile.uniqueId).join()
            val friends = FriendsService.getAllFriendshipsOf(profile.uniqueId).join()
            val party = NetworkPartyService.findParty(profile.uniqueId)

            val anticheatProfile = AnticheatProfileService.findOrFetch(profile.uniqueId).join()
            val anticheatLogs = anticheatProfile?.logs?.map { logs ->
                logs.value.filter {
                    System.currentTimeMillis().minus(it.timestamp) <= TimeUnit.MINUTES.toMillis(5L)
                }
            }?.flatten() ?: emptyList()

            val grants = GrantHandler.fetchGrantsFor(profile.uniqueId).join()
            val punishments = PunishmentHandler.fetchAllPunishmentsForTarget(profile.uniqueId).join()

            PlayerAnalysisMenu(
                PlayerAnalysisContainer(
                    profile = profile,
                    guild = guild,
                    currencies = currencies,
                    playtime = 0L,
                    grants = grants,
                    punishments = punishments,
                    currentParty = party,
                    playerStatus = status.activityDescription,
                    currentMatch = null,
                    anticheatLogs = anticheatLogs,
                    friends,
                    coloredName,
                    QuickAccess.computeRank(profile.uniqueId).join(),
                    CorePlayerProfileService.loadCopyOf(profile.uniqueId).join()
                )
            ).openMenu(sender)
        }


    data class PlayerAnalysisContainer(
        val profile: LemonPlayer,
        val guild: Guild?,
        val currencies: Map<Currency, Long>,
        val playtime: Long,
        val grants: List<Grant>,
        val punishments: List<Punishment>,
        val currentParty: Party?,
        val playerStatus: String,
        val currentMatch: GameReference?,
        val anticheatLogs: List<AnticheatLog>,
        val friends: List<Friendship>,
        val coloredName: String,
        val highestRank: Rank?,
        val corePlayerProfile: CorePlayerProfile?
    )
}
