package gg.tropic.practice.settings

import com.cryptomorin.xseries.XMaterial
import gg.scala.basics.plugin.settings.SettingCategory
import gg.scala.basics.plugin.settings.SettingContainer
import gg.scala.basics.plugin.settings.defaults.values.StateSettingValue
import gg.scala.commons.annotations.plugin.SoftDependency
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan
import gg.tropic.practice.configuration.PracticeConfigurationService
import gg.tropic.practice.friendship.FriendshipStateSetting
import gg.tropic.practice.settings.restriction.RangeRestriction
import gg.tropic.practice.settings.scoreboard.LobbyScoreboardView
import gg.tropic.practice.settings.scoreboard.ScoreboardStyle
import net.evilblock.cubed.scoreboard.ScoreboardListeners
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.visibility.VisibilityHandler
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.scoreboard.DisplaySlot

/**
 * @author GrowlyX
 * @since 10/16/2022
 */
@Service
@IgnoreAutoScan
@SoftDependency("ScBasics")
object DuelsSettingCategory : SettingCategory
{
    const val DUEL_SETTING_PREFIX = "tropicprac"

    override val items = listOf(
        SettingContainer.buildEntry {
            id = "$DUEL_SETTING_PREFIX:duel-requests-fr"
            displayName = "Duel requests"

            clazz = FriendshipStateSetting::class.java
            default = FriendshipStateSetting.Enabled

            description += "Allows you to toggle settings"
            description += "for incoming duel requests."

            item = ItemBuilder.of(XMaterial.DIAMOND_SWORD)
        },
        SettingContainer.buildEntry {
            id = "$DUEL_SETTING_PREFIX:duel-sounds"
            displayName = "Duel sounds"

            clazz = StateSettingValue::class.java
            default = StateSettingValue.ENABLED

            description += "Allows you to be played a"
            description += "notification sound when"
            description += "you receive a duel request."

            item = ItemBuilder.of(XMaterial.JUKEBOX)
        },
        SettingContainer.buildEntry {
            id = "$DUEL_SETTING_PREFIX:allow-spectators"
            displayName = "Allow spectators"

            clazz = StateSettingValue::class.java
            default = StateSettingValue.ENABLED

            description += "Allows you to prevent"
            description += "players from spectating"
            description += "any of your matches."

            item = ItemBuilder.of(XMaterial.GLASS_BOTTLE)
        },
        SettingContainer.buildEntry {
            id = "$DUEL_SETTING_PREFIX:lobby-scoreboard-view"
            displayName = "Lobby scoreboard view"

            clazz = LobbyScoreboardView::class.java
            default = LobbyScoreboardView.None

            description += "Select an extra-info"
            description += "category for your lobby"
            description += "scoreboard"

            displayPredicate = {
                it.hasPermission("practice.lobby.scoreboard.views")
            }

            item = ItemBuilder.of(XMaterial.BOOK)
        },
        SettingContainer.buildEntry {
            id = "$DUEL_SETTING_PREFIX:spawn-visibility-def"
            displayName = "Player visibility"

            clazz = StateSettingValue::class.java
            default = StateSettingValue.ENABLED

            description += "Allows you to view or"
            description += "hide players at spawn."

            postChange = {
                VisibilityHandler.update(it)
            }

            item = ItemBuilder.of(XMaterial.ENDER_EYE)
        },
        SettingContainer.buildEntry {
            id = "$DUEL_SETTING_PREFIX:join-message"
            displayName = "Lobby Join Message"

            clazz = StateSettingValue::class.java
            default = StateSettingValue.ENABLED

            description += "Let players know"
            description += "when you join the lobby."

            displayPredicate = {
                it.hasPermission("minigames.lobby-join-notifications")
            }

            item = ItemBuilder.of(XMaterial.SLIME_BALL)
        },
        SettingContainer.buildEntry {
            id = "$DUEL_SETTING_PREFIX:lobby-flight"
            displayName = "Lobby Flight"

            clazz = StateSettingValue::class.java
            default = StateSettingValue.ENABLED

            description += "Automatically enables"
            description += "flight at spawn."

            displayPredicate = {
                it.hasPermission("practice.spawn-flight")
            }

            item = ItemBuilder.of(XMaterial.FEATHER)
        },
        SettingContainer.buildEntry {
            id = "$DUEL_SETTING_PREFIX:chat-visibility"
            displayName = "Chat visibility"

            clazz = ChatVisibility::class.java
            default = ChatVisibility.Global

            description += "Filter chat messages you"
            description += "see in game."

            item = ItemBuilder.of(XMaterial.PAPER)
        },
        SettingContainer.buildEntry {
            id = "$DUEL_SETTING_PREFIX:restriction-ping"
            displayName = "Ping range"

            clazz = RangeRestriction::class.java
            default = RangeRestriction.None

            description += "Select the maximum value"
            description += "your opponent's ping can"
            description += "differ by."
            description += ""
            description += "${CC.WHITE}Range: ${CC.GREEN}ping ± value"

            displayPredicate = {
                it.hasPermission("practice.ranked.restriction.ping")
            }

            item = ItemBuilder.of(XMaterial.EXPERIENCE_BOTTLE)
        },
        SettingContainer.buildEntry {
            id = "$DUEL_SETTING_PREFIX:silent-spectator"
            displayName = "Silent Spectator"

            clazz = StateSettingValue::class.java
            default = StateSettingValue.ENABLED

            description += "Prevents other players"
            description += "from viewing you as a spectator."

            displayPredicate = {
                it.hasPermission("practice.silent-spectator")
            }

            item = ItemBuilder.of(XMaterial.POTION)
        },
        SettingContainer.buildEntry {
            id = "$DUEL_SETTING_PREFIX:scoreboard-style"
            displayName = "Scoreboard Style"

            clazz = ScoreboardStyle::class.java
            default = ScoreboardStyle.Default

            description += "Allows you to"
            description += "change your scoreboard style."

            displayPredicate = {
                it.hasPermission("practice.style.donator")
            }

            postChange = {
                if (layout(it) == ScoreboardStyle.Disabled)
                {
                    it.scoreboard = Bukkit
                        .getScoreboardManager()
                        .newScoreboard
                } else
                {
                    if (it.scoreboard.getObjective(DisplaySlot.SIDEBAR) == null)
                    {
                        ScoreboardListeners.onPlayerJoinEvent(
                            event = PlayerJoinEvent(it, "")
                        )
                    }
                }
            }

            item = ItemBuilder.of(XMaterial.BREWING_STAND)
        },
        SettingContainer.buildEntry {
            id = "$DUEL_SETTING_PREFIX:auto-accept-quests"
            displayName = "Auto Accept Quests"

            clazz = StateSettingValue::class.java
            default = StateSettingValue.ENABLED

            description += "Automatically accepts all daily"
            description += "and weekly quests."

            displayPredicate = {
                it.hasPermission("minigame.quests.autoactivate")
            }

            item = ItemBuilder
                .of(XMaterial.MAP)
                .glow()
        }
    )

    override fun display(player: Player) = true

    override val displayName = "Minigames"
    override val description = listOf(
        "Duels/minigames style, privacy, matchmaking,",
        "perks, and other options."
    )
}
