package mc.arch.minigames.parties.command.list

import gg.scala.lemon.util.SplitUtil
import mc.arch.minigames.parties.model.PartyStatus
import mc.arch.minigames.parties.service.NetworkPartyService
import mc.arch.minigames.parties.toDisplayName
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.buttons.TexturedHeadButton
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 1/13/2024
 */
class ManagePartiesMenu : PaginatedMenu()
{
    init
    {
        autoUpdate = true
    }

    override fun getMaxItemsPerPage(player: Player) = 36
    override fun size(buttons: Map<Int, Button>) = 45

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        var indexCounter = 0

        return NetworkPartyService.loadedParties()
            .map { reference ->
                ItemBuilder
                    .copyOf(
                        object : TexturedHeadButton("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzVlMzEzZTMwYzUzZGUxNzZlN2YzY2ZjYzI3ODI3ZmQ0NWUxN2QwYzRiOTljNmMxZmI1MmE3MGFiMjkzOTMyNCJ9fX0="){}
                            .getButtonItem(player)
                    )
                    .name("${CC.GREEN}${reference.leader.uniqueId.toDisplayName()}${CC.GREEN}'s Party")
                    .addToLore(
                        "${CC.D_GRAY}ID #${SplitUtil.splitUuid(reference.uniqueId)}",
                        "",
                        "${CC.YELLOW}Status: ${CC.WHITE}${when (reference.status)
                        {
                            PartyStatus.PRIVATE -> "${CC.RED}Private"
                            PartyStatus.PUBLIC -> "${CC.GREEN}Public"
                            PartyStatus.PROTECTED -> "${CC.GOLD}Passcode Required"
                        }}",
                        "${CC.YELLOW}Limit: ${CC.WHITE}${reference.limit}",
                        "",

                    )
                    .addToLore(
                        "${CC.B_YELLOW}Members ${CC.GRAY}(${reference.includedMembers().size})${CC.B_YELLOW}:${
                            if (reference.includedMembers().isEmpty()) "${CC.RED} None!" else ""
                        }"
                    )
                    .apply {
                        if (reference.includedMembers().isNotEmpty())
                        {
                            reference.includedMembers().forEach { player ->
                                addToLore("${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.WHITE}${
                                    player.toDisplayName()
                                }")
                            }
                        }
                    }
                    .addToLore(
                        "",
                        "${CC.GREEN}Click to hijack!",
                    )
                    .toButton { _, type ->
                        Button.playNeutral(player)
                        player.closeInventory()
                        player.performCommand("party hijack ${reference.leader.uniqueId}")
                    }
            }
            .associateBy { indexCounter++ }
    }

    override fun getPrePaginatedTitle(player: Player) = "All Network Parties"
}
