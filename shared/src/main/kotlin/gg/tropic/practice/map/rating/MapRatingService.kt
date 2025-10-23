package gg.tropic.practice.map.rating

import com.mongodb.client.model.Filters
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import gg.tropic.practice.map.Map
import gg.tropic.practice.map.MapService
import gg.tropic.practice.namespace
import net.evilblock.cubed.ScalaCommonsSpigot
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.evilblock.cubed.util.bukkit.Tasks
import net.md_5.bungee.api.chat.ClickEvent
import org.bson.Document
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Class created on 1/12/2024

 * @author 98ping
 * @project tropic-practice
 * @website https://solo.to/redis
 */
@Service
object MapRatingService
{
    val averageRatings = mutableMapOf<String, Double>()

    @Configure
    fun configure()
    {
        Tasks.asyncTimer(0L, 30 * 20L) {
            MapService.cached().maps.values.forEach {
                averageRatings[it.name] = loadAverageRating(it)
            }
        }
    }

    fun sendMapRatingRequest(player: Player, map: Map): CompletableFuture<Void>
    {
        return findExistingMapRating(player.uniqueId, map)
            .thenAccept { rating ->
                if (rating == null)
                {
                    val message = FancyMessage()
                        .withMessage(
                            " ${CC.AQUA}Give us feedback on ${CC.GOLD}${map.displayName}${CC.AQUA} by clicking"
                        )

                    val secondMessage = FancyMessage()
                        .withMessage("${CC.AQUA} one of the stars: ")

                    for (entry in StarRating.entries)
                    {
                        with(secondMessage) {
                            withMessage("${entry.format} ")
                            andHoverOf("${CC.GRAY}Rate ${entry.format}${CC.GRAY}!")
                            andCommandOf(ClickEvent.Action.RUN_COMMAND, "/rate ${map.name} ${entry.name}")
                        }
                    }

                    with(ScalaCommonsSpigot.instance.kvConnection.sync()) {
                        psetex("${namespace()}:ratings:${player.uniqueId}", 1000L * 60L, map.name)
                    }

                    message.sendToPlayer(player)
                    secondMessage.sendToPlayer(player)

                    player.sendMessage("")
                }
            }
    }

    fun findExistingMapRating(player: UUID, map: Map) = DataStoreObjectControllerCache
        .findNotNull<MapRating>()
        .mongo()
        .loadWithFilter(
            Filters.and(
                Filters.eq("mapID", map.name),
                Filters.eq("rater", player.toString())
            )
        )

    fun loadAverageRating(map: Map) = DataStoreObjectControllerCache
        .findNotNull<MapRating>()
        .mongo()
        .aggregate(
            listOf(
                Document(
                    "\$match",
                    Document(
                        "mapID", map.name
                    )
                ),
                Document(
                    "\$group",
                    Document(
                        mapOf(
                            "_id" to "\$_id",
                            "average" to Document(
                                "\$avg", "\$rating"
                            )
                        )
                    )
                )
            )
        )
        .first()
        ?.getDouble("average")
        ?: 0.0

    fun create(rating: MapRating) = DataStoreObjectControllerCache
        .findNotNull<MapRating>()
        .save(rating, DataStoreStorageType.MONGO)
}
