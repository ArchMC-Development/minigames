package gg.tropic.practice.commands.hostedworlds.list

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.util.QuickAccess.username
import gg.scala.lemon.util.SplitUtil
import gg.scala.staff.commands.StaffJumpCommand
import gg.tropic.practice.metadata.SystemMetadataService
import gg.tropic.practice.ugc.HostedWorldState
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
class HostedWorldInstancesMenu : PaginatedMenu()
{
    private var sort = HostedWorldInstanceSort.Type

    init
    {
        autoUpdate = true
        async = true
    }

    override fun asyncLoadResources(player: Player, callback: (Boolean) -> Unit)
    {
        callback(true)
    }

    override fun getMaxItemsPerPage(player: Player) = 36
    override fun size(buttons: Map<Int, Button>) = 45
    override fun getGlobalButtons(player: Player) = mapOf(
        4 to ItemBuilder
            .of(XMaterial.HOPPER)
            .name("${CC.GREEN}Sort")
            .addToLore(
                "",
                *HostedWorldInstanceSort.entries
                    .map { sort ->
                        if (sort == this.sort)
                        {
                            return@map "${CC.AQUA}${Constants.ARROW_RIGHT} ${sort.name}"
                        }

                        return@map "${CC.GRAY}  ${sort.name}"
                    }
                    .toTypedArray(),
                "",
                "${CC.AQUA}Right-click to go backwards!",
                "${CC.YELLOW}Click to switch sort!"
            )
            .toButton { _, type ->
                sort = if (type!!.isRightClick)
                {
                    sort.previous()
                } else
                {
                    sort.next()
                }

                Button.playNeutral(player)
                openMenu(player)
            },
    )

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        var indexCounter = 0

        return sort.sort(SystemMetadataService.allHostedWorldInstances())
            .map { reference ->
                ItemBuilder
                    .copyOf(
                        object : TexturedHeadButton(reference.type.playerHeadValue){}
                            .getButtonItem(player)
                    )
                    .name("${CC.GREEN}${reference.nameId}")
                    .addToLore(
                        "${CC.D_GRAY}GID #${SplitUtil.splitUuid(reference.globalId)}",
                        "${CC.YELLOW}Type: ${CC.WHITE}${reference.type}",
                        "${CC.YELLOW}Uptime: ${CC.WHITE}${
                            TimeUtil.formatMillisIntoAbbreviatedString(System.currentTimeMillis() - reference.loadTime)
                        }",
                        "${CC.YELLOW}Server: ${CC.WHITE}${reference.server}",
                        "${CC.YELLOW}State: ${when (reference.state)
                        {
                            HostedWorldState.ACTIVE -> "${CC.GREEN}Active"
                            HostedWorldState.DECOMMISSIONING -> "${CC.RED}Decommissioning"
                            HostedWorldState.DRAINING -> "${CC.GOLD}Draining"
                        }}",
                        "",
                        "${CC.YELLOW}Owner: ${
                            reference.ownerPlayerId.toDisplayName()
                        }",

                    )
                    .addToLore(
                        "",
                        "${CC.B_YELLOW}Players:${
                            if (reference.onlinePlayers.isEmpty()) "${CC.RED} None!" else ""
                        }"
                    )
                    .apply {
                        if (reference.onlinePlayers.isNotEmpty())
                        {
                            reference.onlinePlayers.forEach { player ->
                                addToLore("${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.WHITE}${player.username()}")
                            }
                        }
                    }
                    .addToLore(
                        "",
                        "${CC.GREEN}Click to visit!",
                    )
                    .toButton { _, type ->
                        player.performCommand("jump ${reference.ownerPlayerId}")
                    }
            }
            .associateBy { indexCounter++ }
    }

    override fun getPrePaginatedTitle(player: Player) = "Hosted World Instances"
}
