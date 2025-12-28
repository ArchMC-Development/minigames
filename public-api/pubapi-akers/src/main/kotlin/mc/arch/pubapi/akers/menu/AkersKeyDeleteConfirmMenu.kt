package mc.arch.pubapi.akers.menu

import com.cryptomorin.xseries.XMaterial
import mc.arch.pubapi.akers.model.AkersApiKey
import mc.arch.pubapi.akers.model.AkersProfile
import mc.arch.pubapi.akers.service.AkersProfileService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player

/**
 * Confirmation menu for deleting an API key.
 *
 * @author Subham
 * @since 12/27/24
 */
class AkersKeyDeleteConfirmMenu(
    private val profile: AkersProfile,
    private val key: AkersApiKey
) : Menu("Delete ${key.name}?")
{
    override fun size(buttons: Map<Int, Button>) = 27

    override fun getButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        // Warning message
        buttons[4] = ItemBuilder
            .of(XMaterial.BARRIER)
            .name("${CC.RED}${CC.BOLD}Delete API Key?")
            .addToLore(
                "",
                "${CC.GRAY}You are about to delete:",
                "${CC.WHITE}${key.name}",
                "",
                "${CC.RED}${CC.BOLD}WARNING:",
                "${CC.RED}This action cannot be undone!",
                "${CC.RED}Any applications using this",
                "${CC.RED}key will stop working."
            )
            .toButton()

        // Confirm button
        buttons[11] = ItemBuilder
            .of(XMaterial.LIME_WOOL)
            .name("${CC.GREEN}${CC.BOLD}Confirm Delete")
            .addToLore(
                "",
                "${CC.GRAY}Click to permanently",
                "${CC.GRAY}delete this API key."
            )
            .toButton { _, _ ->
                profile.revokeKey(key.token, player.uniqueId)
                AkersProfileService.save(profile)

                player.sendMessage("${CC.GREEN}API key ${CC.WHITE}${key.name} ${CC.GREEN}has been deleted.")
                AkersMainMenu(profile).openMenu(player)
            }

        // Cancel button
        buttons[15] = ItemBuilder
            .of(XMaterial.RED_WOOL)
            .name("${CC.RED}${CC.BOLD}Cancel")
            .addToLore(
                "",
                "${CC.GRAY}Click to go back",
                "${CC.GRAY}without deleting."
            )
            .toButton { _, _ ->
                AkersKeyDetailMenu(profile, key).openMenu(player)
            }

        return buttons
    }
}
