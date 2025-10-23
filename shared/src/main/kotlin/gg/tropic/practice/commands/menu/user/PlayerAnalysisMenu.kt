package gg.tropic.practice.commands.menu.user

import com.cryptomorin.xseries.XMaterial
import gg.scala.basics.plugin.profile.BasicsProfileService
import gg.scala.commons.playerstatus.canVirtuallySee
import gg.scala.lemon.handler.PunishmentHandler
import gg.scala.lemon.handler.RankHandler
import gg.scala.lemon.menu.grant.GrantViewMenu
import gg.scala.lemon.menu.punishment.PunishmentViewMenu
import gg.scala.lemon.player.enums.HistoryViewType
import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.lemon.sessions.Session
import gg.scala.lemon.sessions.SessionService
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.username
import gg.tropic.practice.Globals
import gg.tropic.practice.commands.admin.user.AnalyzePlayerCommand
import gg.tropic.practice.games.GameState
import gg.tropic.practice.extensions.toShortString
import mc.arch.minigames.parties.model.PartyStatus
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.menu.menus.ConfirmMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ColorUtil
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.math.Numbers
import net.evilblock.cubed.util.time.TimeUtil
import org.apache.commons.lang.time.DurationFormatUtils
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import java.time.*
import java.time.temporal.TemporalAdjusters
import java.util.*

/**
 * Class created on 7/11/2025

 * @author Max C.
 * @project arch-minigames
 * @website https://solo.to/redis
 */
class PlayerAnalysisMenu(private val container: AnalyzePlayerCommand.PlayerAnalysisContainer) :
    Menu(container.profile.name)
{
    init
    {
        placeholder = true
    }

    override fun size(buttons: Map<Int, Button>): Int = 45

    override fun getButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf<Int, Button>().also { map ->
            map[19] = ItemBuilder.of(XMaterial.PLAYER_HEAD)
                .owner(container.profile.name)
                .name(container.coloredName)
                .toButton()

            val rank = container.highestRank ?: RankHandler.getDefaultRank()
            map[20] = ItemBuilder
                .of(
                    XMaterial.WHITE_WOOL
                )
                .data(
                    (ColorUtil.CHAT_COLOR_TO_WOOL_DATA[
                        ChatColor.getByChar(rank.color[1]) ?: ChatColor.WHITE
                    ]?.toByte() ?: 1).toShort()
                )
                .name("${CC.GRAY}Highest Rank: ${rank.color}${rank.displayName}")
                .toButton()

            map[13] = ItemBuilder.of(XMaterial.GOLD_INGOT)
                .name("${CC.B_AQUA}Currencies")
                .addToLore(
                    "",
                    "${CC.GRAY}Total Currency Amounts:"
                )
                .apply {
                    val currencyMap = container.currencies

                    currencyMap.forEach {
                        this.addToLore(
                            "${it.key.color}${it.key.name}${CC.GRAY}: ${it.key.color}${it.key.symbol} ${
                                Numbers.format(
                                    it.value
                                )
                            }"
                        )
                    }
                }.addToLore(
                    "",
                ).toButton()

            map[14] = ItemBuilder.of(XMaterial.ANVIL)
                .name("${CC.B_AQUA}Punishments")
                .addToLore(
                    "",
                    "${CC.YELLOW}Counts:",
                    "${CC.GRAY}Total Count: ${CC.WHITE}${container.punishments.size}",
                    "${CC.GRAY}Total Active: ${CC.WHITE}${container.punishments.count { it.isActive }}",
                    "${CC.GRAY}Category Disparity:"
                ).apply {
                    container.punishments.map { it.category }.toSet().associateWith { category ->
                        container.punishments.count { it.category == category }
                    }.forEach {
                        this.addToLore(
                            "${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${it.key.color}${it.key.name}${CC.GRAY}: ${CC.WHITE}${it.value}"
                        )
                    }
                }
                .addToLore(
                    "",
                    "${CC.YELLOW}ChatML:",
                    "${CC.GRAY}Amount Flagged: ${CC.WHITE}${
                        container.punishments.count {
                            it.category == PunishmentCategory.MUTE && it.addedBy == null
                        }
                    }",
                    "${CC.GRAY}Convicted Count: ${CC.WHITE}${
                        container.punishments.count {
                            it.category == PunishmentCategory.MUTE && it.addedBy == null && it.removedBy == null && it.isRemoved
                        }
                    }",
                    "",
                    "${CC.GREEN}Left-Click to view audited ChatML logs",
                    "${CC.YELLOW}Right-Click to view history menu"
                ).toButton { _, click ->
                    if (click?.isRightClick == true)
                    {
                        PunishmentHandler.fetchPunishmentsRemovedBy(container.profile.uniqueId).thenAccept { removed ->
                            PunishmentViewMenu(
                                container.profile.uniqueId,
                                HistoryViewType.TARGET_HIST,
                                container.punishments,
                                removed,
                                container.coloredName
                            ).openMenu(player)
                        }
                    } else
                    {
                        player.performCommand("chatml-review ${container.profile.name}")
                    }
                }

            map[15] = ItemBuilder.of(XMaterial.WHITE_WOOL)
                .name("${CC.B_AQUA}Grants")
                .addToLore(
                    "",
                    "${CC.GRAY}Total Count: ${CC.WHITE}${container.grants.size}",
                    "${CC.GRAY}Total Active: ${CC.WHITE}${container.grants.count { it.isActive }}",
                    "${CC.GRAY}Rank Disparity:"
                ).apply {
                    container.grants.map { it.rankId }.toSet().associateWith { category ->
                        container.grants.count { it.rankId == category }
                    }.forEach {
                        this.addToLore(
                            "${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${
                                RankHandler.findRank(it.key)?.getColoredName(false) ?: "${CC.RED}Removed"
                            }${CC.GRAY}: ${CC.WHITE}${it.value}"
                        )
                    }
                }
                .addToLore(
                    "",
                    "${CC.YELLOW}Left-Click to view grant history"
                ).toButton { _, _ ->
                    val grants = container.grants
                    if (grants.isEmpty())
                    {
                        player.sendMessage("${CC.RED}No grants found!")
                        return@toButton
                    }

                    GrantViewMenu(
                        container.profile.uniqueId, HistoryViewType.TARGET_HIST, grants, container.coloredName
                    ).openMenu(player)
                }

            map[16] = ItemBuilder.of(XMaterial.OAK_SIGN)
                .name("${CC.B_AQUA}Player Status")
                .addToLore(
                    if (QuickAccess.online(container.profile.uniqueId)
                            .join() && player.canVirtuallySee(container.profile.uniqueId)
                    ) "${container.coloredName} ${CC.GRAY}is ${CC.AQUA}${container.playerStatus}" else "${CC.RED}User is currently offline!"
                ).toButton()

            map[22] = ItemBuilder.of(XMaterial.JUKEBOX)
                .name("${CC.B_AQUA}Party")
                .apply {
                    val party = container.currentParty

                    if (party == null)
                    {
                        this.addToLore(
                            "${CC.RED}Currently not in a party!"
                        )
                    } else
                    {
                        this.addToLore("")
                        this.addToLore(
                            "${CC.GRAY}Host: ${
                                QuickAccess.computeColoredName(
                                    party.leader.uniqueId,
                                    party.leader.uniqueId.username()
                                ).join()
                            }"
                        )
                        this.addToLore(
                            "${CC.GRAY}Members: ${CC.WHITE}${party.members.size}${CC.D_GRAY}/${CC.WHITE}${party.limit}"
                        )
                        this.addToLore(
                            "${CC.GRAY}Status: ${if (party.status == PartyStatus.PUBLIC) "${CC.GREEN}Open" else "${CC.RED}Closed"}"
                        )
                        this.addToLore(" ")
                    }
                }.toButton()

            map[23] = ItemBuilder.of(XMaterial.HOPPER)
                .name("${CC.B_AQUA}Guild")
                .apply {
                    val guild = container.guild

                    if (guild == null)
                    {
                        this.addToLore(
                            "${CC.RED}Currently not in a guild"
                        )
                    } else
                    {
                        this.addToLore("")
                        this.addToLore("${CC.GRAY}Name: ${CC.WHITE}${guild.name}")
                        this.addToLore(
                            "${CC.GRAY}Owner: ${
                                QuickAccess.computeColoredName(
                                    guild.creator.uniqueId,
                                    guild.creator.uniqueId.username()
                                ).join()
                            }"
                        )
                        this.addToLore(
                            "${CC.GRAY}Members: ${CC.WHITE}${guild.members.size}"
                        )
                        this.addToLore(
                            "${CC.GRAY}All Invite: ${if (guild.allInvite) "${CC.GREEN}Yes" else "${CC.RED}No"}"
                        )
                        this.addToLore(" ")
                    }
                }.toButton()

            map[24] = ItemBuilder.of(XMaterial.CLOCK)
                .name("${CC.B_AQUA}Playtime")
                .addToLore(
                    "${CC.GRAY}${TimeUtil.formatMillisIntoAbbreviatedString(
                        BasicsProfileService
                            .loadCopyOf(container.profile.uniqueId)
                            .join()
                            ?.playtime()
                            ?: 0L
                    )}"
                )
                .toButton()

            map[25] = ItemBuilder.of(XMaterial.BLAZE_POWDER)
                .name("${CC.B_AQUA}Friendships")
                .apply {
                    val friends = container.friends

                    if (friends.isEmpty())
                    {
                        this.addToLore(
                            "${CC.RED}This user has no friends. Poor guy :("
                        )
                    } else
                    {
                        this.addToLore("")
                        friends.forEach {
                            val friend = it.otherEndOf(container.profile.uniqueId)
                            val coloredName = QuickAccess.computeColoredName(friend, friend.username()).join()
                            this.addToLore(
                                "${CC.GRAY}- $coloredName",
                            )
                        }
                        this.addToLore(" ")
                    }
                }.toButton()
            map[31] = ItemBuilder.of(XMaterial.REDSTONE_TORCH)
                .name("${CC.B_AQUA}Anticheat Logs")
                .apply {
                    val logs = container.anticheatLogs.take(5)
                    this.addToLore(
                        "${CC.GRAY}Last 5 Logs:"
                    )

                    if (logs.isEmpty())
                    {
                        this.addToLore(
                            "${CC.RED}None Found",
                        )
                    } else
                    {
                        logs.forEach {
                            this.addToLore(
                                "${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.YELLOW}${it.check}",
                                "${CC.D_GRAY}${CC.ITALIC}On ${it.server}",
                            )
                        }

                        if (logs.size > 5)
                        {
                            this.addToLore(
                                "${CC.GRAY}(And ${logs.size - 5} more...)",
                            )
                        }
                    }

                    this.addToLore(
                        "",
                        "${CC.YELLOW}Left-Click to view logs menu!"
                    )
                }
                .toButton { _, _ ->
                    player.performCommand("logs ${container.profile.name}")
                }
            val coreProfile = container.corePlayerProfile
            map[32] = ItemBuilder.of(XMaterial.EXPERIENCE_BOTTLE)
                .name("${CC.B_AQUA}Leveling")
                .apply {
                    if (coreProfile != null)
                    {
                        this.addToLore(" ")
                        val bedwarsLevel = coreProfile.getLevelInfo("bedwars")
                        this.addToLore(
                            "${CC.RED}Bedwars Level:",
                            "${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.WHITE}${bedwarsLevel.formattedDisplay}",
                            "${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.GREEN}${
                                bedwarsLevel.currentXP.toLong().toShortString()
                            }${CC.D_GRAY}/${CC.AQUA}${bedwarsLevel.xpRequiredForNext.toLong().toShortString()}"
                        )
                        this.addToLore(" ")
                    } else
                    {
                        this.addToLore(
                            "${CC.RED}No leveling information found"
                        )
                    }
                }.toButton()

            // shoutout match list button
            val reference = container.currentMatch
            map[33] = if (reference == null) ItemBuilder.of(XMaterial.CHEST).name("${CC.B_AQUA}Current Match")
                .addToLore("${CC.RED}Player is not currently in a match!").toButton() else
                ItemBuilder
                    .of(XMaterial.CHEST)
                    .name("${CC.B_AQUA}Current Match")
                    .addToLore(
                        "${CC.GRAY}Server: ${CC.WHITE}${reference.server}",
                        "${CC.GRAY}State: ${
                            when (reference.state)
                            {
                                GameState.Waiting -> "${CC.GOLD}Waiting"
                                GameState.Starting -> "${CC.YELLOW}Starting"
                                GameState.Playing -> "${CC.GREEN}Playing"
                                GameState.Completed -> "${CC.D_GREEN}Completed"
                            }
                        }",
                        "",
                        "${CC.GRAY}Queue: ${CC.WHITE}${
                            reference.queueId ?: "${CC.RED}Private"
                        }",
                        "",
                        "${CC.GRAY}Map: ${CC.AQUA}${reference.mapID}",
                        "${CC.GRAY}Kit: ${CC.AQUA}${reference.kitID}",
                        "",
                        "${CC.GREEN}Spectators:${
                            if (reference.spectators.isEmpty()) "${CC.RED} None!" else ""
                        }"
                    )
                    .apply {
                        if (reference.spectators.isNotEmpty())
                        {
                            reference.spectators.forEach { spectator ->
                                addToLore("${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.WHITE}${spectator.username()}")
                            }
                        }
                    }
                    .addToLore(
                        "",
                        "${CC.AQUA}Players:${
                            if (reference.players.isEmpty()) "${CC.RED} None!" else ""
                        }"
                    )
                    .apply {
                        if (reference.players.isNotEmpty())
                        {
                            reference.players.forEach { player ->
                                addToLore(
                                    "${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.WHITE}${
                                        if (player in Globals.POSSIBLE_PLAYER_BOT_UNIQUE_IDS) "${CC.I_WHITE}Robot" else player.username()
                                    }"
                                )
                            }
                        }
                    }
                    .addToLore(
                        "",
                        "${CC.GREEN}[Click to spectate]",
                        "${CC.RED}[Shift-Click to terminate]"
                    )
                    .toButton { _, type ->
                        if (type!!.isShiftClick)
                        {
                            ConfirmMenu(
                                title = "Confirm match termination",
                                confirm = true,
                                callback = { confirmed ->
                                    if (!confirmed)
                                    {
                                        Tasks.sync { openMenu(player) }
                                        return@ConfirmMenu
                                    }

                                    player.performCommand(
                                        "terminatematch ${reference.players.first().username()}"
                                    )
                                }
                            ).openMenu(player)
                            return@toButton
                        }

                        player.performCommand(
                            "spectate ${reference.players.first().username()}"
                        )
                    }
        }
    }

    private fun startOfDay(): ZonedDateTime
    {
        return ZonedDateTime
            .now(ZoneOffset.UTC)
            .toLocalDate()
            .atStartOfDay(ZoneOffset.UTC)
    }

    private fun endOfDay(): ZonedDateTime
    {
        return ZonedDateTime
            .now(ZoneOffset.UTC)
            .toLocalDate()
            .atTime(LocalTime.MAX)
            .atZone(ZoneOffset.UTC)
    }

}
