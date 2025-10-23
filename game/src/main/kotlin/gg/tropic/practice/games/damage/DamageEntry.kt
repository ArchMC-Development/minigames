package gg.tropic.practice.games.damage

import org.bukkit.Material
import org.bukkit.entity.Projectile
import org.bukkit.event.entity.EntityDamageEvent
import java.util.UUID

data class DamageEntry(
    val damager: UUID?,
    val damagerName: String?,
    val cause: EntityDamageEvent.DamageCause,
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val location: org.bukkit.Location,
    val weapon: Material? = null,
    val projectileType: Class<out Projectile>? = null,
    val environmentalContext: EnvironmentalContext? = null
)
{
    fun isExpired(timeoutMs: Long = 10000L): Boolean
    {
        return System.currentTimeMillis() - timestamp > timeoutMs
    }

    fun isRecentlyDamaged(withinMs: Long = 5000L): Boolean
    {
        return System.currentTimeMillis() - timestamp <= withinMs
    }
}
