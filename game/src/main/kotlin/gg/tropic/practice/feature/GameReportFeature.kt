package gg.tropic.practice.feature

import gg.scala.flavor.service.Service
import gg.tropic.practice.games.GameReport
import gg.tropic.practice.namespace
import gg.tropic.practice.suffixWhenDev
import net.evilblock.cubed.ScalaCommonsSpigot
import net.evilblock.cubed.serializers.Serializers
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * @author GrowlyX
 * @since 1/2/2023
 */
@Service
object GameReportFeature
{
    private val connection = ScalaCommonsSpigot.instance.kvConnection

    fun saveSnapshotForAllParticipants(snapshot: GameReport, longTerm: Boolean = false): CompletableFuture<Void>
    {
        return CompletableFuture
            .runAsync {
                connection.sync().setex(
                    "${namespace().suffixWhenDev()}:snapshots:matches:${snapshot.identifier}",
                    if (longTerm) 60L * 60L * 24L * 7L else 60L,
                    Serializers.gson.toJson(snapshot)
                )
            }
            .thenRun {
                listOf(snapshot.winners, snapshot.losers)
                    .flatten()
                    .forEach {
                        connection.sync().setex(
                            "${namespace().suffixWhenDev()}:snapshots:players:$it:matches:${snapshot.identifier}",
                            if (longTerm) 60L * 60L * 24L * 7L else 60L, snapshot.identifier.toString()
                        )
                    }
            }
            .exceptionally {
                it.printStackTrace()
                return@exceptionally null
            }
    }
}
