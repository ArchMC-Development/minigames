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
import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import org.bukkit.entity.Player
import java.text.SimpleDateFormat
import java.util.*

/**
 * Menu for viewing individual API key details.
 *
 * @author Subham
 * @since 12/27/24
 */
class AkersKeyDetailMenu(
    private val profile: AkersProfile,
    private val key: AkersApiKey
) : Menu("Key: ${key.name}")
{
    init
    {
        updateAfterClick = true
    }

    override fun size(buttons: Map<Int, Button>) = 27

    override fun getButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        val dailyRequests = AkersMetricsService.getDailyRequests(key.token)
        val weeklyRequests = AkersMetricsService.getTotalRequests(key.token, 7)

        // Key info (center)
        buttons[13] = ItemBuilder
            .of(XMaterial.TRIPWIRE_HOOK)
            .name("${CC.GREEN}${CC.BOLD}${key.name}")
            .addToLore(
                "",
                "${CC.GRAY}Full Token:",
                "${CC.WHITE}amc-akers_*******",
                "",
                "${CC.GRAY}Created: ${CC.WHITE}${formatTime(key.createdAt)}",
                if (key.lastUsedAt != null)
                    "${CC.GRAY}Last Used: ${CC.WHITE}${formatTime(key.lastUsedAt!!)}"
                else
                    "${CC.GRAY}Last Used: ${CC.WHITE}Never",
            )
            .toButton()

        // Usage stats
        buttons[11] = ItemBuilder
            .of(XMaterial.PAPER)
            .name("${CC.AQUA}${CC.BOLD}Usage Statistics")
            .addToLore(
                "",
                "${CC.GRAY}Requests Today: ${CC.WHITE}$dailyRequests",
                "${CC.GRAY}Requests (7 days): ${CC.WHITE}$weeklyRequests",
                "",
                "${CC.GRAY}Rate Limit: ${CC.WHITE}100/min"
            )
            .toButton()

        // Rename button
        buttons[15] = ItemBuilder
            .of(XMaterial.NAME_TAG)
            .name("${CC.YELLOW}${CC.BOLD}Rename Key")
            .addToLore(
                "",
                "${CC.GRAY}Current Name: ${CC.WHITE}${key.name}",
                "",
                "${CC.YELLOW}Click to rename"
            )
            .toButton { player, _ ->
                player!!.closeInventory()

                InputPrompt()
                    .withText("${CC.GREEN}Enter a new name for your API key:")
                    .acceptInput { _, input ->
                        if (input.length < 3 || input.length > 32)
                        {
                            player!!.sendMessage("${CC.RED}Key name must be between 3 and 32 characters.")
                            return@acceptInput
                        }

                        key.name = input
                        AkersProfileService.save(profile)

                        player!!.sendMessage("${CC.GREEN}API key renamed to: ${CC.WHITE}$input")

                        AkersKeyDetailMenu(profile, key).openMenu(player)
                    }
                    .start(player!!)
            }

        // Delete button
        buttons[22] = ItemBuilder
            .of(XMaterial.RED_DYE)
            .name("${CC.RED}${CC.BOLD}Delete Key")
            .addToLore(
                "",
                "${CC.GRAY}Permanently delete",
                "${CC.GRAY}this API key.",
                "",
                "${CC.RED}This cannot be undone!",
                "",
                "${CC.YELLOW}Click to delete"
            )
            .toButton { _, _ ->
                AkersKeyDeleteConfirmMenu(profile, key).openMenu(player)
            }

        // Back button
        buttons[18] = ItemBuilder
            .of(XMaterial.ARROW)
            .name("${CC.RED}Back")
            .addToLore(
                "${CC.GRAY}Return to main menu"
            )
            .toButton { _, _ ->
                AkersMainMenu(profile).openMenu(player)
            }

        return buttons
    }

    private fun formatTime(timestamp: Long): String
    {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm")
        return sdf.format(Date(timestamp))
    }
}
