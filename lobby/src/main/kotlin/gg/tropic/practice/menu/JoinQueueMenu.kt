package gg.tropic.practice.menu

import gg.scala.basics.plugin.profile.BasicsProfileService
import gg.scala.basics.plugin.settings.defaults.values.StateSettingValue
import gg.scala.commons.playerstatus.isVirtuallyInvisibleToSomeExtent
import gg.scala.staff.ScalaStaffPlugin
import gg.tropic.practice.kit.Kit
import gg.tropic.practice.kit.feature.FeatureFlag
import gg.tropic.practice.leaderboards.CommonlyViewedLeaderboardType
import gg.tropic.practice.player.LobbyPlayerService
import gg.tropic.practice.player.PlayerState
import gg.tropic.practice.profile.PracticeProfileService
import gg.tropic.practice.queue.QueueService
import gg.tropic.practice.queue.QueueType
import gg.tropic.practice.queue.queueId
import gg.tropic.practice.metadata.SystemMetadataService
import gg.tropic.practice.services.LeaderboardManagerService
import gg.tropic.practice.statistics.StatisticID
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.math.Numbers
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

/**
 * @author GrowlyX
 * @since 9/24/2023
 */
class JoinQueueMenu(
    player: Player,
    private val queueType: QueueType,
    private val teamSize: Int
) : TemplateKitMenu(player, dynamic = true)
{
    init
    {
        autoUpdate = true
    }

    override fun getItemAmount(player: Player, kit: Kit) = SystemMetadataService
        .getPlaying(queueId {
            kit(kit)
            queueType(queueType)
            teamSize(teamSize)
        })
        .coerceIn(1..64)

    override fun getAutoUpdateTicks() = 500L
    override fun filterDisplayOfKit(player: Player, kit: Kit) = kit.queueSizes
        .any {
            it.first == teamSize && queueType in it.second
        }

    override fun itemTitleFor(player: Player, kit: Kit) = "${CC.PRI}${kit.displayName}${
        if (kit.features(FeatureFlag.NewlyCreated)) " ${CC.B_AQUA}NEW!" else ""
    }"

    override fun itemDescriptionOf(player: Player, kit: Kit): List<String>
    {
        val queueId = "${kit.id}:${queueType.name}:${teamSize}v${teamSize}"
        val playing = SystemMetadataService.getPlaying(queueId)
        val queued = SystemMetadataService.getQueued(queueId)

        return listOf(
            "${CC.GRAY}Playing: ${CC.PRI}$playing",
            "${CC.GRAY}Queuing: ${CC.PRI}$queued",
            "",
            *topLeaderboardConciseDescription(
                player,
                (if (queueType == QueueType.Ranked) CommonlyViewedLeaderboardType.ELO else CommonlyViewedLeaderboardType.CasualWinStreak)
                    .toStatisticID(kit),
                if (queueType == QueueType.Ranked) "ELO" else "Daily Streak"
            ).toTypedArray(),
            "",
            "${CC.GREEN}Click to queue!"
        )
    }

    private fun topLeaderboardConciseDescription(player: Player, leaderboardID: StatisticID, label: String): List<String>
    {
        val statistic = PracticeProfileService.find(player)
            ?.getStatisticValue(leaderboardID)
            ?: return LeaderboardManagerService
                .getCachedFormattedLeaderboards(leaderboardID.toId())
                .take(3)

        val personalScore = listOf(
            "${CC.PRI}Your $label: ${CC.WHITE}${
                Numbers.format(statistic.score.toLong())
            } ${
                if (statistic.value != -1L) "${CC.GRAY}(#${
                    Numbers.format(statistic.value + 1)
                })" else ""
            }"
        )

        return personalScore + LeaderboardManagerService
            .getCachedFormattedLeaderboards(leaderboardID.toId())
            .take(3)
    }

    override fun itemClicked(player: Player, kit: Kit, type: ClickType)
    {
        val lobbyPlayer = LobbyPlayerService
            .find(player.uniqueId)
            ?: return

        if (lobbyPlayer.state != PlayerState.Idle)
        {
            player.sendMessage("${CC.RED}You cannot join a queue right now!")
            return
        }

        if (player.isVirtuallyInvisibleToSomeExtent())
        {
            player.sendMessage("${CC.RED}You are currently in vanish! Use ${CC.B}/vanish${CC.RED} to be able to join a queue.")
            return
        }

        val basicsProfile = BasicsProfileService.find(player)
        if (basicsProfile != null && player.hasPermission(ScalaStaffPlugin.STAFF_NODE))
        {
            if (basicsProfile.setting("auto-vanish", StateSettingValue.DISABLED) == StateSettingValue.ENABLED)
            {
                player.sendMessage("${CC.RED}You currently have AutoVanish enabled! Use ${CC.B}/toggleautovanish${CC.RED} to be able to join a queue.")
                return
            }
        }

        player.closeInventory()
        QueueService.joinQueue(kit, queueType, teamSize, player)

        Button.playNeutral(player)
        player.sendMessage(
            "${CC.GREEN}You have joined the ${CC.D_GREEN}${queueType.name} ${teamSize}v$teamSize ${kit.displayName}${CC.GREEN} queue!"
        )
    }

    override fun getPrePaginatedTitle(player: Player) =
        "Queueing ${queueType.name} ${teamSize}v$teamSize..."
}
