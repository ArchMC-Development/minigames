package mc.arch.pubapi.akers.command

import gg.scala.commons.acf.CommandHelp
import gg.scala.commons.acf.annotation.*
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import mc.arch.pubapi.akers.menu.AkersStaffLookupMenu
import mc.arch.pubapi.akers.model.AkersProfile
import mc.arch.pubapi.akers.service.AkersProfileService
import net.evilblock.cubed.util.CC
import org.bukkit.command.CommandSender
import java.util.*

/**
 * Staff-facing command for AKERS administration.
 *
 * @author Subham
 * @since 12/27/24
 */
@AutoRegister
@CommandAlias("akersadmin|aadmin")
@CommandPermission("akers.admin")
object AkersAdminCommand : ScalaCommand()
{
    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp) = help.showHelp()

    @Subcommand("lookup")
    @CommandCompletion("@players")
    @Description("View a player's AKERS profile")
    fun onLookup(
        sender: CommandSender,
        target: AsyncLemonPlayer
    ) = target.validatePlayers(sender, true) { lemonPlayer ->
        AkersProfileService
            .loadCopyOf(lemonPlayer.uniqueId)
            .thenAccept { profile ->
                if (profile == null)
                {
                    sender.sendMessage("${CC.RED}This player has no AKERS profile!")
                    return@thenAccept
                }

                if (sender is org.bukkit.entity.Player)
                {
                    AkersStaffLookupMenu(profile, lemonPlayer.name).openMenu(sender)
                } else
                {
                    sender.sendMessage("${CC.GREEN}AKERS Profile for ${CC.WHITE}${lemonPlayer.name}")
                    sender.sendMessage("${CC.GRAY}UUID: ${CC.WHITE}${profile.id}")
                    sender.sendMessage("${CC.GRAY}Discord Linked: ${CC.WHITE}${profile.isLinked()}")
                    if (profile.isLinked())
                    {
                        sender.sendMessage("${CC.GRAY}Discord: ${CC.WHITE}${profile.discordUsername ?: profile.discordId}")
                    }
                    sender.sendMessage("${CC.GRAY}API Keys: ${CC.WHITE}${profile.getActiveKeys().size}/${AkersProfile.MAX_API_KEYS}")
                    sender.sendMessage("${CC.GRAY}Banned: ${CC.WHITE}${profile.banned}")
                    if (profile.banned)
                    {
                        sender.sendMessage("${CC.GRAY}Ban Reason: ${CC.WHITE}${profile.banReason}")
                    }
                }
            }
            .join()
    }

    @Subcommand("ban")
    @CommandCompletion("@players")
    @Description("Ban a player from using AKERS")
    fun onBan(
        sender: CommandSender,
        target: AsyncLemonPlayer,
        reason: String
    ) = target.validatePlayers(sender, true) { lemonPlayer ->
        AkersProfileService
            .loadCopyOf(lemonPlayer.uniqueId)
            .thenAccept { profile ->
                if (profile == null)
                {
                    sender.sendMessage("${CC.RED}This player has no AKERS profile!")
                    return@thenAccept
                }
                if (profile.banned)
                {
                    sender.sendMessage("${CC.RED}${lemonPlayer.name} is already banned from AKERS.")
                    return@thenAccept
                }

                profile.banned = true
                profile.banReason = reason
                profile.bannedBy = if (sender is org.bukkit.entity.Player) sender.uniqueId else null
                profile.bannedAt = System.currentTimeMillis()

                // Revoke all API keys
                profile.revokeAllKeys(profile.bannedBy ?: UUID.fromString("00000000-0000-0000-0000-000000000000"))

                AkersProfileService.save(profile)

                sender.sendMessage("${CC.GREEN}Successfully banned ${CC.WHITE}${lemonPlayer.name} ${CC.GREEN}from AKERS.")
                sender.sendMessage("${CC.GRAY}Reason: ${CC.WHITE}$reason")
                sender.sendMessage("${CC.GRAY}All API keys have been revoked.")
            }
    }

    @Subcommand("unban")
    @CommandCompletion("@players")
    @Description("Unban a player from AKERS")
    fun onUnban(
        sender: CommandSender,
        target: AsyncLemonPlayer
    ) = target.validatePlayers(sender, true) { lemonPlayer ->
        AkersProfileService
            .loadCopyOf(lemonPlayer.uniqueId)
            .thenAccept { profile ->
                if (profile == null)
                {
                    sender.sendMessage("${CC.RED}This player has no AKERS profile!")
                    return@thenAccept
                }
                if (!profile.banned)
                {
                    sender.sendMessage("${CC.RED}${lemonPlayer.name} is not banned from AKERS.")
                    return@thenAccept
                }

                profile.banned = false
                profile.banReason = null
                profile.bannedBy = null
                profile.bannedAt = null

                AkersProfileService.save(profile)

                sender.sendMessage("${CC.GREEN}Successfully unbanned ${CC.WHITE}${lemonPlayer.name} ${CC.GREEN}from AKERS.")
            }
    }

    @Subcommand("revoke")
    @CommandCompletion("@players")
    @Description("Revoke all API keys for a player")
    fun onRevoke(
        sender: CommandSender,
        target: AsyncLemonPlayer
    ) = target.validatePlayers(sender, true) { lemonPlayer ->
        AkersProfileService
            .loadCopyOf(lemonPlayer.uniqueId)
            .thenAccept { profile ->
                if (profile == null)
                {
                    sender.sendMessage("${CC.RED}This player has no AKERS profile!")
                    return@thenAccept
                }
                val activeKeys = profile.getActiveKeys()

                if (activeKeys.isEmpty())
                {
                    sender.sendMessage("${CC.RED}${lemonPlayer.name} has no active API keys.")
                    return@thenAccept
                }

                val revokedBy = if (sender is org.bukkit.entity.Player) sender.uniqueId
                else UUID.fromString("00000000-0000-0000-0000-000000000000")

                profile.revokeAllKeys(revokedBy)
                AkersProfileService.save(profile)

                sender.sendMessage("${CC.GREEN}Revoked ${CC.WHITE}${activeKeys.size} ${CC.GREEN}API key(s) for ${CC.WHITE}${lemonPlayer.name}${CC.GREEN}.")
            }
    }

    @Subcommand("reset")
    @CommandCompletion("@players")
    @Description("Fully reset a player's AKERS profile")
    fun onReset(
        sender: CommandSender,
        target: AsyncLemonPlayer
    ) = target.validatePlayers(sender, true) { lemonPlayer ->
        AkersProfileService
            .loadCopyOf(lemonPlayer.uniqueId)
            .thenAccept { profile ->
                if (profile == null)
                {
                    sender.sendMessage("${CC.RED}This player has no AKERS profile!")
                    return@thenAccept
                }
                val revokedBy = if (sender is org.bukkit.entity.Player) sender.uniqueId
                else UUID.fromString("00000000-0000-0000-0000-000000000000")

                // Revoke all keys
                profile.revokeAllKeys(revokedBy)

                // Unlink Discord
                profile.discordId = null
                profile.discordUsername = null
                profile.linkedAt = null

                // Clear ban
                profile.banned = false
                profile.banReason = null
                profile.bannedBy = null
                profile.bannedAt = null

                AkersProfileService.save(profile)

                sender.sendMessage("${CC.GREEN}Successfully reset AKERS profile for ${CC.WHITE}${lemonPlayer.name}${CC.GREEN}.")
            }
    }
}
