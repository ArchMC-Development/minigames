package gg.tropic.practice.player

import gg.scala.queue.shared.models.Queue
import gg.scala.queue.spigot.stream.SpigotRedisService
import gg.tropic.practice.kit.KitService
import gg.tropic.practice.metadata.SystemMetadataService
import gg.tropic.practice.minigame.MinigameLobby
import gg.tropic.practice.party.WParty
import gg.tropic.practice.player.hotbar.LobbyHotbarService.get
import gg.tropic.practice.queue.QueueService
import gg.tropic.practice.queue.QueueState
import gg.tropic.practice.queue.QueueType
import gg.tropic.practice.queue.toQueueState
import mc.arch.minigames.parties.service.NetworkPartyService
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import java.util.*

data class LobbyPlayer(
    val uniqueId: UUID
)
{
    var maintainStateTimeout = -1L

    val stateUpdateLock = Any()
    var state: PlayerState = PlayerState.None
        set(value)
        {
            field = value

            if (field != PlayerState.None)
            {
                val bukkit = Bukkit.getPlayer(uniqueId)
                    ?: return

                get(value)
                    .applyToPlayer(bukkit)
            }
        }

    private var networkQueue: Queue? = null
    private var queueState: QueueState? = null
    private var party: WParty? = null

    fun queueEntry() = queueState!!.entry
    fun inQueue() = queueState != null
    fun isNetworkQueue() = state == PlayerState.InNetworkQueue

    fun validateQueueEntry() = queueState != null
    fun queuedForTime() = System.currentTimeMillis() - (queueState?.entry?.joinQueueTimestamp ?: System.currentTimeMillis())
    fun queuedForKit() = KitService.cached().kits[queueState?.kitId ?: ""]
    fun queuedForType() = queueState?.queueType ?: QueueType.Casual
    fun queuedForTeamSize() = queueState?.teamSize ?: 1

    fun isInParty() = party != null
    fun partyOf() = party!!

    var hasSyncedInitialQueueState = false
    fun syncQueueStateIfRequired()
    {
        if (!hasSyncedInitialQueueState)
        {
            return
        }

        syncQueueState()
    }

    fun syncQueueState()
    {
        val player = Bukkit.getPlayer(uniqueId)
            ?: return

        queueState = uniqueId.toQueueState()

        val userNetworkQueue = if (Bukkit.getPluginManager().isPluginEnabled("ScQueue"))
        {
            SpigotRedisService.findQueuePlayerIsIn(player)
        } else
        {
            null
        }

        val userParty = NetworkPartyService.findParty(uniqueId)
        val userInParty = userParty != null
        val newState = when (true)
        {
            (userNetworkQueue != null) ->
            {
                networkQueue = userNetworkQueue
                PlayerState.InNetworkQueue
            }

            userInParty ->
            {
                if (this.party != null)
                {
                    party?.update(userParty)
                }

                if (userParty.leader.uniqueId == uniqueId)
                    PlayerState.InPartyAsLeader else PlayerState.InPartyAsMember
            }

            (queueState != null) -> PlayerState.InQueue
            else -> PlayerState.Idle
        }

        // keep current state until the server processes our queue join
        // request and actually updates the queue entry
        if (maintainStateTimeout > System.currentTimeMillis())
        {
            // if we notice that the server pushed whatever state we're expecting (whether it's a queue
            // join/leave that we set temporarily), we'll remove the timeout and continue with our day.
            if (newState == state)
            {
                maintainStateTimeout = -1L
            } else
            {
                return
            }
        }

        if (newState == PlayerState.InPartyAsLeader || newState == PlayerState.InPartyAsMember)
        {
            party = WParty(userParty!!)
            val playerIDs = party!!.onlinePlayers()
            party!!.currentPlayers = playerIDs.size
            party!!.currentPlayersIDs = playerIDs

            if (!(MinigameLobby.isMinigameLobby() || MinigameLobby.isMainLobby()))
            {
                when (true)
                {
                    (queueState != null) -> QueueService
                        .leaveQueue(
                            player, true
                        )

                    else ->
                    {
                    }
                }
            }
        } else
        {
            party = null
        }

        // don't try to acquire lock if we don't need to
        if (newState != state)
        {
            synchronized(stateUpdateLock) {
                // check if the state has changed before we acquired the lock
                if (newState != state)
                {
                    state = newState
                }
            }
        }
    }

    fun findAndJoinRandomQueue()
    {
        val player = Bukkit.getPlayer(uniqueId) ?: return

        if (state != PlayerState.Idle)
        {
            player.sendMessage("${CC.RED}You cannot join a queue right now!")

            return
        }

        val queueType = QueueType.Casual

        val kits = KitService.cached().kits.values.shuffled()

        for (kit in kits)
        {
            for (queueSize in kit.queueSizes)
            {
                if (queueType !in queueSize.second) continue

                val teamSize = queueSize.first
                val queueId = "${kit.id}:${queueType.name}:${teamSize}v${teamSize}"

                if (SystemMetadataService.getQueued(queueId) == 1)
                {
                    QueueService.joinQueue(kit, queueType, teamSize, player)
                    return
                }
            }
        }

        player.sendMessage("${CC.RED}No ${queueType.name} queues with players waiting found!")
    }
}
