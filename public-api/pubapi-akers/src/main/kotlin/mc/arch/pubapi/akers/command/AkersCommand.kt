package mc.arch.pubapi.akers.command

import gg.scala.commons.acf.CommandHelp
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.*
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import mc.arch.pubapi.akers.menu.AkersMainMenu
import mc.arch.pubapi.akers.model.AkersProfile
import mc.arch.pubapi.akers.service.AkersProfileService
import mc.arch.pubapi.akers.service.DiscordOAuthService
import me.lucko.helper.Schedulers
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.md_5.bungee.api.chat.ClickEvent
import java.util.concurrent.CompletableFuture

/**
 * Player-facing command for API key management.
 *
 * @author Subham
 * @since 12/27/24
 */
@AutoRegister
@CommandAlias("api")
object AkersCommand : ScalaCommand()
{
    @Default
    fun onDefault(player: ScalaPlayer): CompletableFuture<*>
    {
        return AkersProfileService.loadCopyOf(player.uniqueId)
            .thenAccept { profile ->
                if (profile == null)
                {
                    player.sendMessage("${CC.RED}Your API profile has not yet been created!")
                    return@thenAccept
                }

                AkersMainMenu(profile).openMenu(player.bukkit())
            }
    }

    @HelpCommand
    fun onHelp(help: CommandHelp) = help.showHelp()

    @Subcommand("link")
    @Description("Link your Discord account to enable API access")
    fun onLink(player: ScalaPlayer)
    {
        val profile = AkersProfileService.find(player.uniqueId)
            ?: throw ConditionFailedException("You do not have an AKERS profile!")

        if (profile.banned)
        {
            player.sendMessage("${CC.RED}You are banned from using the API system.")
            player.sendMessage("${CC.RED}Reason: ${CC.WHITE}${profile.banReason ?: "No reason provided"}")
            return
        }

        if (profile.isLinked())
        {
            player.sendMessage("${CC.RED}Your Discord account is already linked!")
            player.sendMessage("${CC.GRAY}Discord: ${CC.WHITE}${profile.discordUsername ?: profile.discordId}")
            player.sendMessage("${CC.GRAY}Use ${CC.WHITE}/api unlink ${CC.GRAY}to unlink.")
            return
        }

        val token = DiscordOAuthService.createToken(player.uniqueId)
        val url = DiscordOAuthService.buildOAuthUrl(token)

        player.sendMessage("")
        player.sendMessage("${CC.GREEN}${CC.BOLD}Discord Linking")
        player.sendMessage("${CC.GRAY}Click the link below to connect your Discord account:")
        player.sendMessage("")

        val message = FancyMessage()
            .withMessage("${CC.AQUA}${CC.UNDERLINE}$url")
            .andHoverOf("${CC.YELLOW}Click to open in browser")
            .andCommandOf(ClickEvent.Action.OPEN_URL, url)

        message.sendToPlayer(player.bukkit())

        player.sendMessage("")
        player.sendMessage("${CC.YELLOW}This link expires in 5 minutes.")
        player.sendMessage("")
    }

    @Subcommand("unlink")
    @Description("Unlink your Discord account")
    fun onUnlink(player: ScalaPlayer)
    {
        val profile = AkersProfileService.find(player.uniqueId)
            ?: run {
                player.sendMessage("${CC.RED}You don't have an API profile.")
                return
            }

        if (!profile.isLinked())
        {
            player.sendMessage("${CC.RED}Your account is not linked to Discord.")
            return
        }

        val oldDiscord = profile.discordUsername ?: profile.discordId

        // Revoke all API keys when unlinking
        if (profile.getActiveKeys().isNotEmpty())
        {
            profile.revokeAllKeys(player.uniqueId)
            player.sendMessage("${CC.YELLOW}All your API keys have been revoked.")
        }

        profile.discordId = null
        profile.discordUsername = null
        profile.linkedAt = null

        AkersProfileService.save(profile)

        player.sendMessage("${CC.GREEN}Successfully unlinked Discord account: ${CC.WHITE}$oldDiscord")
        player.sendMessage("${CC.GRAY}You can link a new account using ${CC.WHITE}/api link")
    }

    @Subcommand("keys")
    @Description("List your API keys")
    fun onKeys(player: ScalaPlayer)
    {
        val profile = AkersProfileService.find(player.uniqueId)
            ?: run {
                player.sendMessage("${CC.RED}You don't have an API profile.")
                return
            }

        if (!profile.isLinked())
        {
            player.sendMessage("${CC.RED}You must link your Discord account first!")
            player.sendMessage("${CC.GRAY}Use ${CC.WHITE}/api link ${CC.GRAY}to get started.")
            return
        }

        val activeKeys = profile.getActiveKeys()

        if (activeKeys.isEmpty())
        {
            player.sendMessage("${CC.RED}You don't have any API keys.")
            player.sendMessage("${CC.GRAY}Use ${CC.WHITE}/api ${CC.GRAY}to open the management menu.")
            return
        }

        player.sendMessage("")
        player.sendMessage("${CC.GREEN}${CC.BOLD}Your API Keys")
        player.sendMessage("")

        activeKeys.forEachIndexed { index, key ->
            val maskedToken = key.token.take(15) + "..."
            player.sendMessage("${CC.WHITE}${index + 1}. ${CC.GREEN}${key.name}")
            player.sendMessage("   ${CC.GRAY}Token: ${CC.WHITE}$maskedToken")
            player.sendMessage("   ${CC.GRAY}Created: ${CC.WHITE}${formatTime(key.createdAt)}")
            if (key.lastUsedAt != null)
            {
                player.sendMessage("   ${CC.GRAY}Last Used: ${CC.WHITE}${formatTime(key.lastUsedAt!!)}")
            }
        }

        player.sendMessage("")
        player.sendMessage("${CC.GRAY}Keys: ${CC.WHITE}${activeKeys.size}/${AkersProfile.MAX_API_KEYS}")
        player.sendMessage("")
    }

    private fun formatTime(timestamp: Long): String
    {
        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm")
        return sdf.format(java.util.Date(timestamp))
    }
}
