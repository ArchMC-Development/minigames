package gg.tropic.practice.parkour

import me.lucko.helper.metadata.MetadataKey
import me.lucko.helper.metadata.MetadataMap
import me.lucko.helper.metadata.type.PlayerMetadataRegistry
import net.evilblock.cubed.ScalaCommonsSpigot
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import java.util.UUID

/**
 * @author GrowlyX
 * @since 3/18/2025
 */
private val metadata = mutableMapOf<UUID, ParkourPlaySession>()
fun Player.extractPlaySession() = metadata[uniqueId]!!
fun Player.startPlaySession()
{
    metadata[uniqueId] = ParkourPlaySession()
}

fun Player.isPlayingParkour() = metadata.containsKey(uniqueId)
fun Player.endPlaySession()
{
    metadata.remove(uniqueId)
}
