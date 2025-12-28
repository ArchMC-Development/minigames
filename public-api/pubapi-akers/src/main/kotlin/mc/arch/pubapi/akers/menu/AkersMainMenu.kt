package mc.arch.pubapi.akers.menu

import com.cryptomorin.xseries.XMaterial
import mc.arch.pubapi.akers.model.AkersApiKey
import mc.arch.pubapi.akers.model.AkersProfile
import mc.arch.pubapi.akers.service.AkersMetricsService
import mc.arch.pubapi.akers.service.AkersProfileService
import mc.arch.pubapi.akers.service.TokenGenerationService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import net.md_5.bungee.api.chat.ClickEvent
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import java.text.SimpleDateFormat
import java.util.*

/**
 * Main menu for API key management.
 *
 * @author Subham
 * @since 12/27/24
 */
class AkersMainMenu(
    private val profile: AkersProfile
) : Menu("API Management")
{
    init
    {
        updateAfterClick = true
    }

    override fun size(buttons: Map<Int, Button>) = 45

    override fun getButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        // Discord status button (top center)
        buttons[4] = buildDiscordStatusButton(player)

        // API key slots (row 2)
        val activeKeys = profile.getActiveKeys()
        for (i in 0 until AkersProfile.MAX_API_KEYS)
        {
            val slot = 20 + i * 2 // Slots 19, 21, 23

            val key = activeKeys.getOrNull(i)
            buttons[slot] = if (key != null)
            {
                buildKeyButton(key, i + 1)
            }
            else if (profile.isLinked() && !profile.banned)
            {
                buildCreateKeyButton()
            }
            else
            {
                buildLockedSlotButton()
            }
        }

        // Usage stats button (bottom left)
        buttons[36] = buildUsageStatsButton()

        // Documentation button (bottom center)
        buttons[44] = buildDocsButton()

        return buttons
    }

    private fun buildDiscordStatusButton(player: Player): Button
    {
        return if (profile.banned)
        {
            ItemBuilder
                .of(XMaterial.BARRIER)
                .name("${CC.RED}${CC.BOLD}API Access Banned")
                .addToLore(
                    "",
                    "${CC.GRAY}You are banned from",
                    "${CC.GRAY}using the API system.",
                    "",
                    "${CC.RED}Reason: ${CC.WHITE}${profile.banReason ?: "No reason"}",
                    "",
                    "${CC.GRAY}Contact staff if you",
                    "${CC.GRAY}believe this is an error."
                )
                .toButton()
        }
        else if (profile.isLinked())
        {
            ItemBuilder
                .of(XMaterial.EMERALD)
                .name("${CC.GREEN}${CC.BOLD}Discord Linked")
                .addToLore(
                    "",
                    "${CC.GRAY}Connected to:",
                    "${CC.WHITE}${profile.discordUsername ?: profile.discordId}",
                    "",
                    "${CC.GRAY}Linked: ${CC.WHITE}${formatTime(profile.linkedAt!!)}",
                    "",
                    "${CC.YELLOW}Click to unlink"
                )
                .toButton { _, _ ->
                    player.closeInventory()
                    player.performCommand("api unlink")
                }
        }
        else
        {
            ItemBuilder
                .of(XMaterial.REDSTONE)
                .name("${CC.RED}${CC.BOLD}Discord Not Linked")
                .addToLore(
                    "",
                    "${CC.GRAY}Link your Discord account",
                    "${CC.GRAY}to create API keys.",
                    "",
                    "${CC.YELLOW}Click to generate link"
                )
                .toButton { _, _ ->
                    player.closeInventory()
                    player.performCommand("api link")
                }
        }
    }

    private fun buildKeyButton(key: AkersApiKey, index: Int): Button
    {
        val dailyRequests = AkersMetricsService.getDailyRequests(key.token)
        val weeklyRequests = AkersMetricsService.getTotalRequests(key.token, 7)

        return ItemBuilder
            .of(XMaterial.TRIPWIRE_HOOK)
            .name("${CC.GREEN}${CC.BOLD}${key.name}")
            .addToLore(
                "",
                "${CC.GRAY}Token: ${CC.WHITE}${key.token.take(15)}${CC.MAGIC}AAAAAAA",
                "",
                "${CC.GRAY}Created: ${CC.WHITE}${formatTime(key.createdAt)}",
                if (key.lastUsedAt != null)
                    "${CC.GRAY}Last Used: ${CC.WHITE}${formatTime(key.lastUsedAt!!)}"
                else
                    "${CC.GRAY}Last Used: ${CC.WHITE}Never",
                "",
                "${CC.GRAY}Requests Today: ${CC.WHITE}$dailyRequests",
                "${CC.GRAY}Requests (7 days): ${CC.WHITE}$weeklyRequests",
                "",
                "${CC.YELLOW}Left-Click to view details",
                "${CC.RED}Right-Click to delete"
            )
            .toButton { player, click ->
                if (click == ClickType.RIGHT || click == ClickType.SHIFT_RIGHT)
                {
                    AkersKeyDeleteConfirmMenu(profile, key).openMenu(player!!)
                }
                else
                {
                    AkersKeyDetailMenu(profile, key).openMenu(player!!)
                }
            }
    }

    private fun buildCreateKeyButton(): Button
    {
        return ItemBuilder
            .of(XMaterial.LIME_DYE)
            .name("${CC.GREEN}${CC.BOLD}Create API Key")
            .addToLore(
                "",
                "${CC.GRAY}Create a new API key",
                "${CC.GRAY}for external applications.",
                "",
                "${CC.GRAY}Keys: ${CC.WHITE}${profile.getActiveKeys().size}/${AkersProfile.MAX_API_KEYS}",
                "",
                "${CC.YELLOW}Click to create"
            )
            .toButton { player, _ ->
                player!!.closeInventory()

                InputPrompt()
                    .withText("${CC.GREEN}Enter a name for your API key:")
                    .acceptInput { _, input ->
                        if (input.length < 3 || input.length > 32)
                        {
                            player.sendMessage("${CC.RED}Key name must be between 3 and 32 characters.")
                            return@acceptInput
                        }

                        val newKey = TokenGenerationService.createApiKey(input)

                        if (profile.addKey(newKey))
                        {
                            AkersProfileService.save(profile)

                            player.sendMessage("")
                            player.sendMessage("${CC.GREEN}${CC.BOLD}API Key Created!")
                            player.sendMessage("")
                            player.sendMessage("${CC.GRAY}Name: ${CC.WHITE}${newKey.name}")

                            player.sendMessage("${CC.GRAY}CLICK TO COPY TOKEN:")
                            FancyMessage()
                                .withMessage(newKey.token)
                                .andHoverOf("Click to copy token!")
                                .andCommandOf(
                                    ClickEvent.Action.SUGGEST_COMMAND,
                                    newKey.token
                                )
                                .sendToPlayer(player)

                            player.sendMessage("")
                            player.sendMessage("${CC.YELLOW}${CC.BOLD}IMPORTANT: ${CC.YELLOW}Save this token now!")
                            player.sendMessage("${CC.GRAY}You won't be able to see the full token again.")
                            player.sendMessage("")

                            Tasks.delayed(20L) {
                                AkersMainMenu(profile).openMenu(player)
                            }
                        }
                        else
                        {
                            player.sendMessage("${CC.RED}Failed to create API key. You may have reached the limit.")
                        }
                    }
                    .start(player)
            }
    }

    private fun buildLockedSlotButton(): Button
    {
        return ItemBuilder
            .of(XMaterial.GRAY_DYE)
            .name("${CC.GRAY}${CC.BOLD}Locked Slot")
            .setLore(
                if (!profile.isLinked())
                {
                    listOf(
                        "${CC.GRAY}Link your Discord",
                        "${CC.GRAY}account to unlock."
                    )
                }
                else
                {
                    listOf(
                        "${CC.GRAY}This slot is",
                        "${CC.GRAY}currently unavailable."
                    )
                }
            )
            .toButton()
    }

    private fun buildUsageStatsButton(): Button
    {
        val activeKeys = profile.getActiveKeys()
        val totalDaily = activeKeys.sumOf { AkersMetricsService.getDailyRequests(it.token) }
        val totalWeekly = activeKeys.sumOf { AkersMetricsService.getTotalRequests(it.token, 7) }

        return ItemBuilder
            .of(XMaterial.PAPER)
            .name("${CC.AQUA}${CC.BOLD}Usage Statistics")
            .addToLore(
                "",
                "${CC.GRAY}Total API Keys: ${CC.WHITE}${activeKeys.size}/${AkersProfile.MAX_API_KEYS}",
                "",
                "${CC.GRAY}Requests Today: ${CC.WHITE}$totalDaily",
                "${CC.GRAY}Requests (7 days): ${CC.WHITE}$totalWeekly",
                "",
                "${CC.GRAY}Rate Limit: ${CC.WHITE}100/min per key"
            )
            .toButton()
    }

    private fun buildDocsButton(): Button
    {
        return ItemBuilder
            .of(XMaterial.BOOK)
            .name("${CC.GOLD}${CC.BOLD}API Documentation")
            .addToLore(
                "",
                "${CC.GRAY}View the full API",
                "${CC.GRAY}documentation online.",
                "",
                "${CC.WHITE}https://api.arch.mc/swagger-ui/index.html",
                "",
                "${CC.YELLOW}Click to copy URL"
            )
            .toButton { player, _ ->
                player!!.sendMessage("${CC.GREEN}Documentation: ${CC.WHITE}https://api.arch.mc/swagger-ui/index.html")
            }
    }

    private fun formatTime(timestamp: Long): String
    {
        val sdf = SimpleDateFormat("MMM dd, yyyy")
        return sdf.format(Date(timestamp))
    }
}
