package gg.tropic.practice.ugc

import gg.scala.commons.agnostic.sync.ServerSync
import gg.tropic.practice.ugc.resources.HostedWorldInstancePlayerResources
import mc.arch.minigames.versioned.generics.worlds.LoadedSlimeWorld
import me.lucko.helper.terminable.composite.CompositeTerminable
import net.evilblock.cubed.util.CC
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.UUID

/**
 * Core instance interface for Hosted Worlds.
 *
 * Hosted worlds, unlike minigame [gg.tropic.practice.games.AbstractGame]
 * instances, only allow for online players.
 *
 * Lifecycle is nearly identical to the game system.
 *
 * @author Subham
 * @since 7/18/25
 */
interface HostedWorldInstance<R : HostedWorldInstancePlayerResources> : CompositeTerminable
{
    val playerResources: MutableMap<UUID, R>

    val globalId: UUID
    val nameId: String
    val ownerPlayerId: UUID

    val loadedWorld: LoadedSlimeWorld
    val bukkitWorld: World
        get() = loadedWorld.bukkitWorld

    val persistencePolicy: PersistencePolicy
    val persistence: HostedWorldPersistence?
    val providerType: WorldInstanceProviderType
    var state: HostedWorldState

    var hasUnloaded: Boolean
    var lastDrainBroadcast: Long
    var drainStartTime: Long

    var initialTimeWhenEmpty: Long
    val loadTime: Long

    fun load()
    fun unload()

    fun onlinePlayers(): Set<Player>

    fun playerResourcesOf(uniqueId: UUID): R? = playerResources[uniqueId]
    fun playerResourcesOf(player: Player): R

    fun generateScoreboardTitle(player: Player) = "${CC.B_RED}HOSTED WORLD"
    fun generateScoreboardLines(player: Player): List<String>
    {
        return emptyList()
    }

    fun displayNameOf(player: Player) = playerResources
        .getOrPut(player.uniqueId) {
            playerResourcesOf(player)
        }.displayName

    fun reference() = HostedWorldInstanceReference(
        globalId = globalId,
        nameId = nameId,
        ownerPlayerId = ownerPlayerId,
        type = providerType,
        state = state,
        onlinePlayers = onlinePlayers()
            .map(Player::getUniqueId)
            .toSet(),
        server = ServerSync.local.id,
        loadTime = loadTime
    )

    fun playerSpawnLocation(): Location
    {
        return bukkitWorld.spawnLocation
    }

    /**
     * Base events
     */
    fun onLogin(player: Player)
    {

    }

    fun onLogout(player: Player)
    {

    }

}
