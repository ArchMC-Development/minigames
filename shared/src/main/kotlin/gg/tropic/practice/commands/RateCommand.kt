package gg.tropic.practice.commands

import com.cryptomorin.xseries.XSound
import com.mongodb.client.model.Filters
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import gg.tropic.practice.map.Map
import gg.tropic.practice.map.rating.MapRating
import gg.tropic.practice.map.rating.MapRatingService
import gg.tropic.practice.map.rating.StarRating
import gg.tropic.practice.namespace
import net.evilblock.cubed.ScalaCommonsSpigot
import net.evilblock.cubed.util.CC
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 5/28/2024
 */
@AutoRegister
object RateCommand : ScalaCommand()
{
    @CommandAlias("rate")
    fun rate(player: Player, map: Map, rating: StarRating) = CompletableFuture
        .supplyAsync {
            ScalaCommonsSpigot.instance.kvConnection.sync()
                .get("${namespace()}:ratings:${player.uniqueId}")
        }
        .thenComposeAsync { pending ->
            if (pending != map.name)
            {
                throw ConditionFailedException(
                    "You cannot rate this map right now!"
                )
            }

            MapRatingService
                .findExistingMapRating(player.uniqueId, map)
                .thenCompose { existing ->
                    if (existing != null)
                    {
                        throw ConditionFailedException("You have already rated this map!")
                    }

                    val newRating = MapRating(
                        rater = player.uniqueId,
                        mapID = map.name,
                        rating = rating.ordinal + 1
                    )

                    DataStoreObjectControllerCache
                        .findNotNull<MapRating>()
                        .save(newRating, DataStoreStorageType.MONGO)
                }
                .thenRunAsync {
                    player.playSound(player.location, XSound.ENTITY_PLAYER_LEVELUP.parseSound(), 1.0f, 1.0f)
                    player.sendMessage("${CC.GREEN}Thank you for providing feedback for the map ${CC.YELLOW}${map.displayName}${CC.GREEN}!")
                }
        }
}
