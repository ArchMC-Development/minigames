package gg.tropic.practice.region

import gg.tropic.practice.extensions.into
import lol.arch.symphony.api.model.TrackedPlayer
import net.evilblock.cubed.ScalaCommonsSpigot
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 12/17/2023
 */
object PlayerRegionFromRedisProxy
{
    fun of(player: Player): CompletableFuture<Region> = CompletableFuture
        .supplyAsync {
            ScalaCommonsSpigot.instance.kvConnection
                .sync()
                .hget("symphony:players", player.uniqueId.toString())
                ?.into<TrackedPlayer>()
        }
        .thenApply(Region::extractFromTrackedPlayer)
        .exceptionally { Region.NA }

    fun ofPlayerID(player: UUID): CompletableFuture<Region> = CompletableFuture
        .supplyAsync {
            ScalaCommonsSpigot.instance.kvConnection
                .sync()
                .hget("symphony:players", player.toString())
                ?.into<TrackedPlayer>()
        }
        .thenApply(Region::extractFromTrackedPlayer)
        .exceptionally { Region.NA }
}
