package gg.tropic.practice.games.features

import com.cryptomorin.xseries.XMaterial
import gg.tropic.practice.games.GameService
import gg.tropic.practice.games.GameState
import gg.tropic.practice.extensions.PlayerVelocityUtilities
import net.evilblock.cubed.util.CC
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftFireball
import org.bukkit.entity.Fireball
import org.bukkit.entity.Player
import org.bukkit.entity.TNTPrimed
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.*

class FireballFeature : Listener
{
    companion object
    {
        var fireballExplosionSize: Double = 3.0
    }

    private val cooldowns = mutableMapOf<UUID, Long>()

    @EventHandler
    fun dc(event: PlayerQuitEvent)
    {
        cooldowns.remove(event.player.uniqueId)
    }

    @EventHandler
    fun interact(event: PlayerInteractEvent)
    {
        //check hand
        val inHand = event.item ?: return
        val game = GameService.byPlayer(event.player)
            ?: return

        if (event.action == Action.RIGHT_CLICK_BLOCK || event.action == Action.RIGHT_CLICK_AIR)
        {
            if (inHand.type == XMaterial.FIRE_CHARGE.get())
            {
                if (!game.state(GameState.Playing))
                {
                    event.player.sendMessage("${CC.RED}You cannot throw fireballs right now!")
                    return
                }

                event.isCancelled = true

                if (System.currentTimeMillis() - cooldowns.getOrDefault(event.player.uniqueId, 0L) > (0.5 * 1000))
                {
                    cooldowns[event.player.uniqueId] = System.currentTimeMillis()

                    var fireball = event.player.launchProjectile(Fireball::class.java)
                    val direction = event.player.eyeLocation.direction

                    fireball = setFireballDirection(fireball, direction)
                    fireball.velocity = fireball.direction.multiply(10.0)
                    fireball.setIsIncendiary(false)
                    fireball.yield = fireballExplosionSize.toFloat()

                    minusAmount(event.player, inHand, 1)
                }
            }
        }
    }

    fun minusAmount(p: Player, i: ItemStack, amount: Int)
    {
        if (i.amount - amount <= 0)
        {
            p.inventory.removeItem(i)
            return
        }

        i.amount -= amount
        p.updateInventory()
    }

    fun setFireballDirection(fireball: Fireball, vector: Vector): Fireball
    {
        val fb = (fireball as CraftFireball).handle
        fb.dirX = vector.x * 0.1
        fb.dirY = vector.y * 0.1
        fb.dirZ = vector.z * 0.1
        return fb.bukkitEntity as Fireball
    }

    @EventHandler
    fun EntityDamageByEntityEvent.onTNTDirectHit()
    {
        if (entity !is Player)
        {
            return
        }

        if (!(damager is TNTPrimed))
        {
            if (damager is Fireball)
            {
                isCancelled = true
            }
            return
        }

        val player = entity as Player
        val game = GameService.byPlayerOrSpectator(player.uniqueId)
            ?: return

        if (!game.state(GameState.Playing))
        {
            isCancelled = true
            return
        }

        if (GameService.isSpectating(player))
        {
            isCancelled = true
            return
        }

        isCancelled = true
        player.damage(1.0)

        PlayerVelocityUtilities.pushAway(
            player,
            damager.location,
            1.15, 1.2
        )
    }

    @EventHandler
    fun ProjectileHitEvent.onFireballDirectHit()
    {
        if (entity !is Fireball)
        {
            return
        }

        if (entity.shooter !is Player)
        {
            return
        }

        val player = entity.shooter as Player
        val game = GameService.byPlayerOrSpectator(player.uniqueId)
            ?: return

        if (!game.state(GameState.Playing))
        {
            return
        }

        if (GameService.isSpectating(player))
        {
            return
        }

        entity.getNearbyEntities(3.0, 3.0, 3.0)
            .forEach { nearby ->
                if (nearby is Player)
                {
                    if (GameService.isSpectating(nearby))
                    {
                        return@forEach
                    }
                }

                val playerVector = nearby.location.toVector()
                val normalizedVector = entity.location.toVector().subtract(playerVector).normalize()
                val horizontalVector = normalizedVector.multiply(if (game.minigameType() == null) -1.25 else -1.0)
                val vertical = if (game.minigameType() == null) 0.85 else 0.65

                var y = normalizedVector.getY()
                if (y < 0)
                {
                    y += 1.5
                }

                y = if (y <= 0.5)
                {
                    vertical * 1.5
                } else
                {
                    y * vertical * 1.5
                }

                if (nearby is Player)
                {
                    nearby.damage(1.0)
                }

                nearby.velocity = horizontalVector.setY(y)
            }
    }

    /*@EventHandler
    fun ExplosionPrimeEvent.onPrime()
    {
        if (entity !is Fireball) return
        val shooter = (entity as Fireball).shooter ?: return
        if (shooter !is Player) return

        val game = GameService.byPlayer(shooter)
            ?: return run {
                fire = false
            }

        fire = game.miniGameLifecycle != null
    }*/
}
