package mc.arch.pubapi.akers.menu

import com.cryptomorin.xseries.XMaterial
import mc.arch.pubapi.akers.model.AkersApiKey
import mc.arch.pubapi.akers.model.AkersProfile
import mc.arch.pubapi.akers.service.AkersMetricsService
import mc.arch.pubapi.akers.service.AkersProfileService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player
import java.text.SimpleDateFormat
import java.util.*

/**
 * Staff menu for viewing and managing a player's AKERS profile.
 *
 * @author Subham
 * @since 12/27/24
 */
class AkersStaffLookupMenu(
    private val profile: AkersProfile,
    private val playerName: String
) : Menu("$playerName's AKERS Profile")
{
    init
    {
        updateAfterClick = true
    }

    override fun size(buttons: Map<Int, Button>) = 45

    override fun getButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        // Player info (top)
        buttons[4] = buildPlayerInfoButton()

        // Discord info
        buttons[19] = buildDiscordInfoButton()

        // Ban status
        buttons[21] = buildBanStatusButton(player)

        // API Keys section
        buttons[23] = buildApiKeysButton()

        // Individual key buttons
        val activeKeys = profile.getActiveKeys()
        activeKeys.take(3).forEachIndexed { index, key ->
            buttons[28 + index * 2] = buildKeyButton(key, player)
        }

        // Revoke all keys button
        if (activeKeys.isNotEmpty())
        {
            buttons[40] = ItemBuilder
                .of(XMaterial.TNT)
                .name("${CC.RED}${CC.BOLD}Revoke All Keys")
                .addToLore(
                    "",
                    "${CC.GRAY}Revoke all active",
                    "${CC.GRAY}API keys for this player.",
                    "",
                    "${CC.RED}Active Keys: ${CC.WHITE}${activeKeys.size}",
                    "",
                    "${CC.YELLOW}Click to revoke all"
                )
                .toButton { _, _ ->
                    profile.revokeAllKeys(player.uniqueId)
                    AkersProfileService.save(profile)
                    player.sendMessage("${CC.GREEN}Revoked all API keys for ${CC.WHITE}$playerName${CC.GREEN}.")
                    AkersStaffLookupMenu(profile, playerName).openMenu(player)
                }
        }

        return buttons
    }

    private fun buildPlayerInfoButton(): Button
    {
        return ItemBuilder
            .of(XMaterial.PLAYER_HEAD)
            .name("${CC.GREEN}${CC.BOLD}$playerName")
            .addToLore(
                "",
                "${CC.GRAY}UUID: ${CC.WHITE}${profile.id}",
                "",
                "${CC.GRAY}Discord Linked: ${if (profile.isLinked()) "${CC.GREEN}Yes" else "${CC.RED}No"}",
                "${CC.GRAY}API Keys: ${CC.WHITE}${profile.getActiveKeys().size}/${AkersProfile.MAX_API_KEYS}",
                "${CC.GRAY}Banned: ${if (profile.banned) "${CC.RED}Yes" else "${CC.GREEN}No"}"
            )
            .toButton()
    }

    private fun buildDiscordInfoButton(): Button
    {
        return if (profile.isLinked())
        {
            ItemBuilder
                .of(XMaterial.EMERALD)
                .name("${CC.GREEN}${CC.BOLD}Discord Linked")
                .addToLore(
                    "",
                    "${CC.GRAY}Discord ID: ${CC.WHITE}${profile.discordId}",
                    "${CC.GRAY}Username: ${CC.WHITE}${profile.discordUsername ?: "Unknown"}",
                    "${CC.GRAY}Linked At: ${CC.WHITE}${formatTime(profile.linkedAt!!)}"
                )
                .toButton()
        }
        else
        {
            ItemBuilder
                .of(XMaterial.REDSTONE)
                .name("${CC.RED}${CC.BOLD}Discord Not Linked")
                .addToLore(
                    "",
                    "${CC.GRAY}This player has not",
                    "${CC.GRAY}linked their Discord account."
                )
                .toButton()
        }
    }

    private fun buildBanStatusButton(viewer: Player): Button
    {
        return if (profile.banned)
        {
            ItemBuilder
                .of(XMaterial.BARRIER)
                .name("${CC.RED}${CC.BOLD}AKERS Banned")
                .addToLore(
                    "",
                    "${CC.GRAY}Reason: ${CC.WHITE}${profile.banReason ?: "No reason"}",
                    "${CC.GRAY}Banned At: ${CC.WHITE}${formatTime(profile.bannedAt ?: 0)}",
                    "",
                    "${CC.YELLOW}Click to unban"
                )
                .toButton { player, _ ->
                    profile.banned = false
                    profile.banReason = null
                    profile.bannedBy = null
                    profile.bannedAt = null
                    AkersProfileService.save(profile)
                    player!!.sendMessage("${CC.GREEN}Unbanned ${CC.WHITE}$playerName ${CC.GREEN}from AKERS.")
                    AkersStaffLookupMenu(profile, playerName).openMenu(player!!)
                }
        }
        else
        {
            ItemBuilder
                .of(XMaterial.LIME_DYE)
                .name("${CC.GREEN}${CC.BOLD}Not Banned")
                .addToLore(
                    "",
                    "${CC.GRAY}This player is not",
                    "${CC.GRAY}banned from AKERS.",
                    "",
                    "${CC.YELLOW}Click to ban"
                )
                .toButton { player, _ ->
                    player!!.closeInventory()
                    player!!.sendMessage("${CC.YELLOW}Use ${CC.WHITE}/akersadmin ban $playerName <reason> ${CC.YELLOW}to ban.")
                }
        }
    }

    private fun buildApiKeysButton(): Button
    {
        val activeKeys = profile.getActiveKeys()
        val totalDaily = activeKeys.sumOf { AkersMetricsService.getDailyRequests(it.token) }
        val totalWeekly = activeKeys.sumOf { AkersMetricsService.getTotalRequests(it.token, 7) }

        return ItemBuilder
            .of(XMaterial.TRIPWIRE_HOOK)
            .name("${CC.AQUA}${CC.BOLD}API Keys (${activeKeys.size})")
            .addToLore(
                "",
                "${CC.GRAY}Active Keys: ${CC.WHITE}${activeKeys.size}/${AkersProfile.MAX_API_KEYS}",
                "${CC.GRAY}Revoked Keys: ${CC.WHITE}${profile.apiKeys.count { it.revoked }}",
                "",
                "${CC.GRAY}Total Requests Today: ${CC.WHITE}$totalDaily",
                "${CC.GRAY}Total Requests (7d): ${CC.WHITE}$totalWeekly"
            )
            .toButton()
    }

    private fun buildKeyButton(key: AkersApiKey, viewer: Player): Button
    {
        val dailyRequests = AkersMetricsService.getDailyRequests(key.token)

        return ItemBuilder
            .of(XMaterial.PAPER)
            .name("${CC.WHITE}${key.name}")
            .addToLore(
                "",
                "${CC.GRAY}Token: ${CC.WHITE}${key.token.take(15)}...",
                "${CC.GRAY}Created: ${CC.WHITE}${formatTime(key.createdAt)}",
                "${CC.GRAY}Requests Today: ${CC.WHITE}$dailyRequests",
                "",
                "${CC.RED}Right-Click to revoke"
            )
            .toButton { player, click ->
                if (click?.isRightClick == true)
                {
                    profile.revokeKey(key.token, player!!.uniqueId)
                    AkersProfileService.save(profile)
                    player!!.sendMessage("${CC.GREEN}Revoked API key ${CC.WHITE}${key.name} ${CC.GREEN}for ${CC.WHITE}$playerName${CC.GREEN}.")
                    AkersStaffLookupMenu(profile, playerName).openMenu(player)
                }
            }
    }

    private fun formatTime(timestamp: Long): String
    {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm")
        return sdf.format(Date(timestamp))
    }
}
