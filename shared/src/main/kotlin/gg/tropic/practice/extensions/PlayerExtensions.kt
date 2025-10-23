package gg.tropic.practice.extensions

import me.lucko.helper.Helper
import net.minecraft.server.v1_8_R3.EntityBat
import net.minecraft.server.v1_8_R3.PacketPlayOutAttachEntity
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue

/**
 * @author Subham
 * @since 7/23/25
 */
fun Player.mountImmovable()
{
    val craftPlayer = player as CraftPlayer
    val location = player.location

    val bat = EntityBat((location.world as CraftWorld).handle)
    bat.setLocation(location.x, location.y + 0.5, location.z, 0f, 0f)
    bat.isInvisible = true
    bat.health = 6f

    val spawnEntityPacket = PacketPlayOutSpawnEntityLiving(bat)
    craftPlayer.handle.playerConnection.sendPacket(spawnEntityPacket)

    player.setMetadata("seated", FixedMetadataValue(Helper.hostPlugin(), bat.id))

    val sitPacket = PacketPlayOutAttachEntity(0, craftPlayer.handle, bat)
    craftPlayer.handle.playerConnection.sendPacket(sitPacket)
}

fun Player.mountImmovable(location: Location)
{
    val craftPlayer = player as CraftPlayer
    val bat = EntityBat((location.world as CraftWorld).handle)
    bat.setLocation(location.x, location.y + 0.5, location.z, 0f, 0f)
    bat.isInvisible = true
    bat.health = 6f

    val spawnEntityPacket = PacketPlayOutSpawnEntityLiving(bat)
    craftPlayer.handle.playerConnection.sendPacket(spawnEntityPacket)

    player.setMetadata("seated", FixedMetadataValue(Helper.hostPlugin(), bat.id))

    val sitPacket = PacketPlayOutAttachEntity(0, craftPlayer.handle, bat)
//    player.teleport(location)
    craftPlayer.handle.playerConnection.sendPacket(sitPacket)
}

fun Player.unmount()
{
    if (player.hasMetadata("seated"))
    {
        val craftPlayer = player as CraftPlayer
        val packet = PacketPlayOutEntityDestroy(player.getMetadata("seated")[0].asInt())
        craftPlayer.handle.playerConnection.sendPacket(packet)
    }
}

fun Player.addOrDrop(vararg itemStack: org.bukkit.inventory.ItemStack)
{
    val added = inventory.addItem(*itemStack)
    added.forEach {
        world.dropItemNaturally(location, it.value)
    }
}
