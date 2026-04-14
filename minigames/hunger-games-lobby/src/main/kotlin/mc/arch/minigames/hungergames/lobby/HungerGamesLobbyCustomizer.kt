package mc.arch.minigames.hungergames.lobby

import com.cryptomorin.xseries.XMaterial
import gg.scala.basics.plugin.profile.BasicsProfileService
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.tropic.game.extensions.economy.EconomyDataSync
import gg.tropic.game.extensions.economy.EconomyProfileService
import gg.tropic.game.extensions.profile.CorePlayerProfileService
import gg.tropic.practice.extensions.createProgressBar
import gg.tropic.practice.extensions.toShortString
import gg.tropic.practice.minigame.MiniGameTypeProvider
import gg.tropic.practice.minigame.MinigameCompetitiveCustomizer
import gg.tropic.practice.minigame.MinigameLobby
import gg.tropic.practice.minigame.MinigameLobbyCustomizer
import gg.tropic.practice.minigame.MinigameLobbyScoreboardProvider
import gg.tropic.practice.player.LobbyPlayer
import gg.tropic.practice.profile.PracticeProfile
import gg.tropic.practice.profile.PracticeProfileService
import gg.tropic.practice.settings.DuelsSettingCategory
import gg.tropic.practice.settings.scoreboard.LobbyScoreboardView
import mc.arch.minigames.hungergames.HungerGamesTypeMetadata
import mc.arch.minigames.hungergames.lobby.menu.HungerGamesQuickJoinMenu
import mc.arch.minigames.hungergames.lobby.menu.HungerGamesMainMenu
import mc.arch.minigames.hungergames.statistics.formatCoreHolographicStatistics
import mc.arch.minigames.hungergames.statistics.formatStatistics
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.math.Numbers
import org.bukkit.entity.Player

/**
 * @author ArchMC
 */
@Service
object HungerGamesLobbyCustomizer : MinigameLobbyCustomizer, MinigameLobbyScoreboardProvider, MiniGameTypeProvider,
    MinigameCompetitiveCustomizer
{
    override val id: String = "hungergames"
    override fun mainMenuProvider(player: Player)
    {
        HungerGamesMainMenu().openMenu(player)
    }

    override fun playProvider(player: Player)
    {
        HungerGamesQuickJoinMenu().openMenu(player)
    }

    override fun leaderboardsProvider(player: Player) = mapOf<Int, Button>()
    override fun statisticsMenuProvider(profile: PracticeProfile) = mapOf(
        13 to ItemBuilder
            .of(XMaterial.ENDER_EYE)
            .name("${CC.RED}Core Statistics")
            .setLore(
                profile.formatStatistics(null)
            )
            .toButton()
    ) + HungerGamesTypeMetadata.gameModes.values
        .mapIndexed { index, metadata ->
            (28 + (2 * index)) to metadata.toRawItem()
                .name("${CC.GREEN}${metadata.displayName} Statistics")
                .setLore(
                    profile.formatStatistics(metadata)
                )
                .toButton()
        }

    override fun holographicStatsProvider(player: Player) = PracticeProfileService
        .find(player)
        ?.formatCoreHolographicStatistics()
        ?: listOf("${CC.GRAY}???")

    @Configure
    fun configure()
    {
        MinigameLobby.customize(this, this)
    }

    override fun scoreboard() = this

    override fun provideTitle() = "${CC.BD_RED}SG"
    override fun provideIdleLines(
        player: Player,
        lobbyProfile: LobbyPlayer
    ): List<String>
    {
        val lines = mutableListOf(
            "${CC.D_RED}${Constants.THIN_VERTICAL_LINE} ${CC.GRAY}Online: ${CC.WHITE}${
                Numbers.format(
                    HungerGamesTypeMetadata.totalPlayersInLobby() + HungerGamesTypeMetadata.totalPlayersPlaying()
                )
            }",
            "${CC.D_RED}${Constants.THIN_VERTICAL_LINE} ${CC.GRAY}In-game: ${CC.WHITE}${
                Numbers.format(
                    HungerGamesTypeMetadata.totalPlayersPlaying()
                )
            }"
        )

        // Coin balance
        val economyProfile = EconomyProfileService.find(player)
        if (economyProfile != null)
        {
            val economy = EconomyDataSync.cached().economies["hunger-games-coins"]
            if (economy != null)
            {
                lines += "${CC.D_RED}${Constants.THIN_VERTICAL_LINE} ${CC.GRAY}Coins: ${
                    economy.format(economyProfile.balance("hunger-games-coins"))
                }"
            }
        }

        val view = BasicsProfileService.find(player)
            ?.setting(
                "${DuelsSettingCategory.DUEL_SETTING_PREFIX}:lobby-scoreboard-view",
                LobbyScoreboardView.None
            )

        val coreProfile = CorePlayerProfileService.find(player)
        if (coreProfile != null && view == LobbyScoreboardView.None)
        {
            val level = coreProfile.getLevelInfo("hungergames")
            lines += listOf(
                "",
                "${CC.D_RED}Progress:",
                "${CC.D_RED}${Constants.THIN_VERTICAL_LINE} ${CC.GRAY}Level: ${CC.GRAY}${
                    level.formattedDisplay
                }",
                "${CC.D_RED}${Constants.THIN_VERTICAL_LINE} ${CC.GRAY}Progress: ${CC.GREEN}${
                    level.currentXP.toLong().toShortString()
                }${CC.GRAY}/${CC.AQUA}${
                    level.xpRequiredForNext.toLong().toShortString()
                }",
                "${CC.D_RED}${Constants.THIN_VERTICAL_LINE} ${CC.D_GRAY}[${CC.GRAY}${createProgressBar(
                    level.currentXP.toDouble(),
                    level.xpRequiredForNext.toDouble()
                )}${CC.D_GRAY}]",
            )
        }

        return lines
    }

    override fun provide() = HungerGamesTypeMetadata
}
