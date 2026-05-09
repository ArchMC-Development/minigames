package mc.arch.minigames.versioned.legacy

import mc.arch.minigames.versioned.generics.ItemStackProvider
import mc.arch.minigames.versioned.generics.PlayerProvider
import net.evilblock.cubed.util.bukkit.ItemBuilder
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
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.lang.reflect.Field

/**
 * @author Subham
 * @since 8/6/25
 */
object LegacyItemstackProvider : ItemStackProvider
{
    override fun makeUnbreakable(itemStack: ItemStack): ItemStack
    = ItemBuilder.copyOf(itemStack).setUnbreakable(true).build()
}
