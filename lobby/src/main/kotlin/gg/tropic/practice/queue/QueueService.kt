package gg.tropic.practice.queue

import gg.scala.aware.AwareBuilder
import gg.scala.aware.codec.codecs.interpretation.AwareMessageCodec
import gg.scala.aware.message.AwareMessage
import gg.scala.aware.thread.AwareThreadContext
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import mc.arch.minigames.parties.model.Party
import gg.tropic.practice.category.pingRange
import gg.tropic.practice.games.spectate.PlayerSpectateRequest
import gg.tropic.practice.games.spectate.SpectateRequest
import gg.tropic.practice.kit.Kit
import gg.tropic.practice.minigame.MiniGameQueueConfiguration
import gg.tropic.practice.player.LobbyPlayerService
import gg.tropic.practice.player.PlayerState
import gg.tropic.practice.profile.PracticeProfileService
import gg.tropic.practice.region.PlayerRegionFromRedisProxy
import gg.tropic.practice.region.Region
import gg.tropic.practice.statistics.TrackedKitStatistic
import gg.tropic.practice.statistics.statisticIdFrom
import gg.tropic.practice.suffixWhenDev
import me.lucko.helper.Helper
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.nms.MinecraftReflection
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import java.util.logging.Logger

/**
 * @author GrowlyX
 * @since 9/24/2023
 */
@Service
object QueueService
{
    private val aware by lazy {
        AwareBuilder
            .of<AwareMessage>("communications-gamequeue".suffixWhenDev())
            .codec(AwareMessageCodec)
            .logger(Logger.getAnonymousLogger())
            .build()
    }

    fun createMessage(packet: String, vararg pairs: Pair<String, Any?>): AwareMessage =
        AwareMessage.of(packet, this.aware, *pairs)

    @Configure
    fun configure()
    {
        this.aware.connect()
            .toCompletableFuture()
            .join()
    }

    fun leaveQueue(player: Player, force: Boolean = false)
    {
        val lobbyPlayer = LobbyPlayerService
            .find(player.uniqueId)
            ?: return

        createMessage(
            packet = "leave",
            "leader" to player.uniqueId,
        ).publish(
            context = AwareThreadContext.ASYNC
        )

        if (force)
        {
            return
        }

        // set Idle and wait until the queue server syncs
        synchronized(lobbyPlayer.stateUpdateLock) {
            lobbyPlayer.state = PlayerState.Idle
            lobbyPlayer.maintainStateTimeout = System.currentTimeMillis() + 1000L
        }
    }

    fun spectate(request: PlayerSpectateRequest)
    {
        createMessage(
            packet = "spectate",
            "request" to request
        ).publish(
            context = AwareThreadContext.SYNC
        )
    }

    fun joinQueue(kit: Kit, queueType: QueueType, teamSize: Int, player: Player,
                  miniGameQueueConfiguration: MiniGameQueueConfiguration? = null)
    {

        val lobbyPlayer = LobbyPlayerService
            .find(player)
            ?: return

        if (lobbyPlayer.state == PlayerState.InQueue)
        {
            return
        }

        QueueCommunications.joinQueue(
            kit = kit,
            queueType = queueType,
            teamSize = teamSize,
            player = player,
            miniGameQueueConfiguration = miniGameQueueConfiguration
        )

        // set InQueue and wait until the queue server syncs
        synchronized(lobbyPlayer.stateUpdateLock) {
            lobbyPlayer.state = PlayerState.InQueue
            lobbyPlayer.maintainStateTimeout = System.currentTimeMillis() + 1000L
        }
    }
}
