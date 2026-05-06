package mc.arch.minigames.versioned.legacy

import mc.arch.minigames.versioned.generics.PlayerProvider
import net.minecraft.server.v1_8_R3.EntityBat
import net.minecraft.server.v1_8_R3.EntityTNTPrimed
import net.minecraft.server.v1_8_R3.PacketPlayOutAttachEntity
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving
import net.minecraft.server.v1_8_R3.WorldSettings
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftFireball
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftTNTPrimed
import org.bukkit.entity.Entity
import org.bukkit.entity.Fireball
import org.bukkit.entity.Player
import org.bukkit.entity.TNTPrimed
import org.bukkit.util.Vector
import java.lang.reflect.Field

/**
 * @author Subham
 * @since 8/6/25
 */
object LegacyPlayerProvider : PlayerProvider
{
    override fun obfuscateHealth(player: Player, obfuscated: Boolean)
    {
        //player.custom().tracker.isHealthObfuscated = obfuscated
    }

    override fun mountImmovable(player: Player): Int =
        spawnSeatBat(player, player.location)

    override fun mountImmovable(player: Player, location: Location): Int =
        spawnSeatBat(player, location)

    private fun spawnSeatBat(player: Player, location: Location): Int
    {
        val craftPlayer = player as CraftPlayer
        val bat = EntityBat((location.world as CraftWorld).handle)
        bat.setLocation(location.x, location.y + 0.5, location.z, 0f, 0f)
        bat.isInvisible = true
        bat.health = 6f

        craftPlayer.handle.playerConnection.sendPacket(PacketPlayOutSpawnEntityLiving(bat))
        craftPlayer.handle.playerConnection.sendPacket(
            PacketPlayOutAttachEntity(0, craftPlayer.handle, bat)
        )
        return bat.id
    }

    override fun unmount(player: Player, mountId: Int)
    {
        val craftPlayer = player as CraftPlayer
        craftPlayer.handle.playerConnection.sendPacket(PacketPlayOutEntityDestroy(mountId))
    }

    override fun hideNametag(entity: Entity)
    {
        (entity as CraftLivingEntity).handle.dataWatcher.watch<Byte>(9, -1)
    }

    override fun setSurvivalGameMode(player: Player)
    {
        (player as CraftPlayer).handle.playerInteractManager
            .gameMode = WorldSettings.EnumGamemode.SURVIVAL
    }

    override fun freezeTime(player: Player, frozen: Boolean)
    {
        player.custom().isTimeFrozen = frozen
    }

    override fun setFireballDirection(fireball: Fireball, direction: Vector): Fireball
    {
        val handle = (fireball as CraftFireball).handle
        handle.dirX = direction.x * 0.1
        handle.dirY = direction.y * 0.1
        handle.dirZ = direction.z * 0.1
        return handle.bukkitEntity as Fireball
    }

    override fun setTntSource(tnt: TNTPrimed, owner: Player)
    {
        val nmsLiving = (owner as CraftLivingEntity).handle
        val nmsTnt = (tnt as CraftTNTPrimed).handle
        try
        {
            val sourceField: Field = EntityTNTPrimed::class.java.getDeclaredField("source")
            sourceField.isAccessible = true
            sourceField.set(nmsTnt, nmsLiving)
        } catch (ex: Exception)
        {
            ex.printStackTrace()
        }
    }
}
