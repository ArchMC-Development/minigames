package mc.arch.minigames.parties.service

import gg.scala.aware.AwareBuilder
import gg.scala.aware.codec.codecs.interpretation.AwareMessageCodec
import gg.scala.aware.message.AwareMessage
import gg.scala.aware.thread.AwareThreadContext
import gg.scala.commons.ScalaCommonsGlobals
import gg.scala.commons.agnostic.sync.ServerSync
import gg.scala.commons.consensus.elections.LeaderElection
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.redirection.impl.VelocityRedirectSystem
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.username
import mc.arch.minigames.parties.PartiesPlugin
import mc.arch.minigames.parties.model.Party
import mc.arch.minigames.parties.model.PartyMember
import mc.arch.minigames.parties.model.PartyRole
import mc.arch.minigames.parties.service.event.PartyCreateEvent
import mc.arch.minigames.parties.service.event.PartyDisbandEvent
import mc.arch.minigames.parties.service.event.PartyUpdateEvent
import mc.arch.minigames.parties.toDisplayName
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.services.CommonsServiceExecutor
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import org.bukkit.Bukkit
import java.time.Duration
import java.util.*
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * @author Subham
 * @since 7/2/25
 */
@Service
object NetworkPartyService : PartyService
{
    @Inject
    lateinit var plugin: PartiesPlugin

    private val partyLock = ReentrantReadWriteLock()
    private var parties = mutableMapOf<UUID, Party>()

    private val sync by lazy {
        AwareBuilder
            .of<AwareMessage>("minigames:parties")
            .codec(AwareMessageCodec)
            .logger(plugin.logger)
            .build()
    }

    @Configure
    fun configure()
    {
        preLoadAllParties()
        configureSyncReceiver()

        var future: ScheduledFuture<*>? = null
        LeaderElection.withAnyServerSyncServer(
            electionID = "party-expiration",
            refreshRate = Duration.ofMillis(500L),
            elect = {
                future?.cancel(true)
                future = null
                future = CommonsServiceExecutor.scheduleAtFixedRate({
                   runCatching {
                       parties.values.toList().forEach { party ->
                           val offlineMembers = party.includedMembers()
                               .filter { member ->
                                   QuickAccess.online(member).join() == false
                               }

                           val onlineMembers = party.includedMembers()
                               .filter { member ->
                                   member !in offlineMembers
                               }

                           offlineMembers.forEach { player ->
                               // The leader disconnected, and there's another player to take over
                               if (party.leader.uniqueId == player && onlineMembers.isNotEmpty())
                               {
                                   val onlineMember = onlineMembers.first()
                                   party.members.remove(onlineMember)
                                   party.leader.uniqueId = onlineMember
                                   updateParty(party)

                                   party.sendMessage(FancyMessage()
                                       .withMessage("${CC.GRAY}${CC.STRIKE_THROUGH}${" ".repeat(53)}\n")
                                       .withMessage("${CC.RED}The party was transferred to ${onlineMember.toDisplayName()}.\n")
                                       .withMessage("${CC.YELLOW}The previous leader, ${CC.GREEN}${player.toDisplayName()}${CC.YELLOW}, disconnected.\n")
                                       .withMessage("${CC.GRAY}${CC.STRIKE_THROUGH}${" ".repeat(53)}")
                                   )
                               } else
                               {
                                   // A regular member disconnected from the network
                                   if (party.leader.uniqueId != player)
                                   {
                                       party.members.remove(player)
                                       updateParty(party)

                                       party.sendMessage(FancyMessage()
                                           .withMessage("${CC.GRAY}${CC.STRIKE_THROUGH}${" ".repeat(53)}\n")
                                           .withMessage("${CC.RED}${player.toDisplayName()} disconnected from the network.\n")
                                           .withMessage("${CC.YELLOW}They were removed from the party.\n")
                                           .withMessage("${CC.GRAY}${CC.STRIKE_THROUGH}${" ".repeat(53)}")
                                       )
                                   } else
                                   {
                                       // The leader disconnected, and there are no other players in the party online to take over
                                       delete(party)
                                   }
                               }
                           }
                       }
                   }.onFailure { throwable ->
                       throwable.printStackTrace()
                   }
                }, 0L, 500L, TimeUnit.MILLISECONDS)
            },
            resign = {
                future?.cancel(true)
                future = null
            }
        )
    }

    private fun configureSyncReceiver()
    {
        sync.listen("update") {
            val uniqueId = retrieve<UUID>("uniqueId")
            val loadedParty = loadParty(uniqueId)
                ?: return@listen

            partyLock.write {
                parties[uniqueId] = loadedParty
                PartyUpdateEvent(loadedParty).callEvent()
            }
        }

        sync.listen("delete") {
            val uniqueId = retrieve<UUID>("uniqueId")
            partyLock.write {
                parties.remove(uniqueId)?.apply {
                    PartyDisbandEvent(this).callEvent()
                }
            }
        }

        sync.listen("warp") {
            val uniqueId = retrieve<UUID>("uniqueId")
            val party = findPartyByID(uniqueId)
                ?: return@listen

            val server = retrieve<String>("server")
            if (server == ServerSync.local.id)
            {
                return@listen
            }

            for (uuid in party.members.keys)
            {
                val bukkitPlayer = Bukkit
                    .getPlayer(uuid)
                    ?: continue

                bukkitPlayer.sendMessage("${CC.GRAY}${CC.STRIKE_THROUGH}${" ".repeat(53)}")
                bukkitPlayer.sendMessage("${CC.GREEN}Your party is being warped to ${CC.GOLD}$server${CC.GREEN}!")
                bukkitPlayer.sendMessage("${CC.GRAY}${CC.STRIKE_THROUGH}${" ".repeat(53)}")

                VelocityRedirectSystem.redirect(bukkitPlayer, server)
            }
        }

        sync.connect().toCompletableFuture().join()
    }

    private fun preLoadAllParties()
    {
        partyLock.write {
            parties = ScalaCommonsGlobals.redis()
                .sync()
                .hgetall("minigames:parties")
                .mapValues { entry ->
                    Serializers.gson.fromJson(entry.value, Party::class.java)
                }
                .mapKeys { UUID.fromString(it.key) }
                .toMutableMap()

            plugin.logger.info("[cache] Pre loaded ${parties.size} ${if (parties.size == 1) "" else "s"} in memory.")
        }
    }

    private fun loadParty(uniqueId: UUID) = ScalaCommonsGlobals.redis()
        .sync()
        .hget("minigames:parties", uniqueId.toString())
        ?.let {
            Serializers.gson.fromJson(it, Party::class.java)
        }

    override fun findParty(member: UUID) = partyLock.read { parties.values }
        .firstOrNull {
            it.leader.uniqueId == member ||
                member in it.members.keys
        }

    override fun createParty(leader: UUID): Party
    {
        val newParty = Party(leader = PartyMember(
            uniqueId = leader,
            role = PartyRole.LEADER
        ))

        partyLock.write {
            parties[newParty.uniqueId] = newParty
            Bukkit.getPlayer(leader)?.apply {
                PartyCreateEvent(newParty, this).callEvent()
            }
        }

        updateParty(newParty)
        return newParty
    }

    override fun updateParty(party: Party)
    {
        ScalaCommonsGlobals.redis()
            .sync()
            .hset(
                "minigames:parties",
                party.uniqueId.toString(),
                Serializers.gson.toJson(party)
            )

        partyLock.write {
            parties[party.uniqueId] = party
        }

        AwareMessage.of(
            packet = "update",
            aware = sync,
            "uniqueId" to party.uniqueId
        ).publish(
            context = AwareThreadContext.SYNC
        )
    }

    override fun warpPartyHere(party: Party)
    {
        AwareMessage.of(
            packet = "warp",
            aware = sync,
            "uniqueId" to party.uniqueId,
            "server" to ServerSync.local.id,
        ).publish(
            context = AwareThreadContext.ASYNC
        )
    }

    fun warpPartyTo(party: Party, server: String)
    {
        AwareMessage.of(
            packet = "warp",
            aware = sync,
            "uniqueId" to party.uniqueId,
            "server" to server,
        ).publish(
            context = AwareThreadContext.ASYNC
        )
    }

    override fun delete(party: Party)
    {
        ScalaCommonsGlobals.redis()
            .sync()
            .hdel(
                "minigames:parties",
                party.uniqueId.toString()
            )

        partyLock.write {
            parties.remove(party.uniqueId)
        }

        AwareMessage.of(
            packet = "delete",
            aware = sync,
            "uniqueId" to party.uniqueId
        ).publish(
            context = AwareThreadContext.SYNC
        )
    }

    override fun findPartyByID(id: UUID) = partyLock.read {
        parties[id]
    }

    override fun loadedParties() = partyLock.read { parties.values.toList() }
}
