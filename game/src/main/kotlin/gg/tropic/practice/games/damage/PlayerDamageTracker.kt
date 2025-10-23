package gg.tropic.practice.games.damage

import com.cryptomorin.xseries.XMaterial
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.entity.TNTPrimed
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object PlayerDamageTracker
{
    private val damageHistory = ConcurrentHashMap<UUID, MutableList<DamageEntry>>()
    private val combatTags = ConcurrentHashMap<UUID, Long>()

    const val DAMAGE_TIMEOUT_MS = 10000L // 10 seconds
    const val RECENT_DAMAGE_MS = 5000L // 5 seconds for context
    const val COMBAT_TAG_MS = 15000L // 15 seconds combat tag
    const val VOID_Y_LEVEL = 0

    init
    {
        // Clean up expired entries periodically
        Tasks.asyncTimer(0L, 20L * 30L) {
            cleanupExpiredEntries()
        } // Every 30 seconds
    }

    /**
     * Records damage dealt to a player
     */
    fun recordDamage(
        victim: Player,
        event: EntityDamageEvent
    )
    {
        val damagerInfo = when (event)
        {
            is EntityDamageByEntityEvent ->
            {
                val info = extractDamagerInfo(event)
                if (info?.first == victim.uniqueId) null else info
            }
            else -> null
        }

        val environmentalContext = analyzeEnvironmentalContext(victim, event)

        val entry = DamageEntry(
            damager = damagerInfo?.first,
            damagerName = damagerInfo?.second,
            cause = event.cause,
            amount = event.finalDamage,
            location = victim.location.clone(),
            weapon = extractWeapon(event),
            projectileType = extractProjectileType(event),
            environmentalContext = environmentalContext
        )

        damageHistory.computeIfAbsent(victim.uniqueId) { mutableListOf() }.add(entry)

        // Update combat tag if damaged by player
        if (damagerInfo?.first != null)
        {
            combatTags[victim.uniqueId] = System.currentTimeMillis()
            combatTags[damagerInfo.first] = System.currentTimeMillis()
        }

        // Cleanup old entries for this player
        cleanupPlayerEntries(victim.uniqueId)
    }

    /**
     * Determines the elimination cause when a player dies
     */
    fun determineEliminationCause(victim: Player, alternative: EliminationCause): Pair<EliminationCause, UUID?>
    {
        val entries = damageHistory[victim.uniqueId] ?: return Pair(EliminationCause.UNKNOWN, null)

        if (entries.isEmpty())
        {
            return Pair(alternative, null)
        }

        // Get the most recent damage that could be the cause of death
        val recentDamage = entries
            .filter { it.isRecentlyDamaged(RECENT_DAMAGE_MS) }
            .sortedByDescending { it.timestamp }

        if (recentDamage.isEmpty())
        {
            // Check if they died from void
            if (victim.location.y <= VOID_Y_LEVEL)
            {
                return findContextPlayerForCause(EliminationCause.VOID_DAMAGE, entries)
            }
            return Pair(alternative, null)
        }

        // Analyze the damage pattern to determine cause
        val primaryCause = analyzeDamagePattern(recentDamage, victim)
        return findContextPlayerForCause(primaryCause, entries)
            .let { (cause, uUID) ->
                if (cause == EliminationCause.UNKNOWN)
                {
                    return@let alternative to uUID
                }

                return@let cause to uUID
            }
    }

    /**
     * Analyzes damage pattern to determine primary elimination cause
     */
    private fun analyzeDamagePattern(
        recentDamage: List<DamageEntry>,
        victim: Player
    ): EliminationCause
    {
        val mostRecentDamage = recentDamage.first()

        // Check current location context
        val currentY = victim.location.y
        val wasInVoid = currentY <= VOID_Y_LEVEL
        val nearLava = isNearLava(victim.location)
        val nearFire = isNearFire(victim.location)

        // Prioritize by cause and context
        return when
        {
            // Direct player kills (highest priority)
            mostRecentDamage.cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK
                && mostRecentDamage.damager != null -> EliminationCause.KILLED_BY_PLAYER

            mostRecentDamage.cause == EntityDamageEvent.DamageCause.PROJECTILE
                && mostRecentDamage.damager != null -> EliminationCause.PROJECTILE_BY_PLAYER

            // Environmental with context
            wasInVoid -> EliminationCause.VOID_DAMAGE

            mostRecentDamage.cause == EntityDamageEvent.DamageCause.FALL -> EliminationCause.FALL_DAMAGE

            nearLava || mostRecentDamage.cause == EntityDamageEvent.DamageCause.LAVA ->
                EliminationCause.LAVA_DAMAGE

            mostRecentDamage.cause == EntityDamageEvent.DamageCause.DROWNING ->
                EliminationCause.DROWNING

            mostRecentDamage.cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                || mostRecentDamage.cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION ->
                EliminationCause.EXPLOSION

            nearFire || mostRecentDamage.cause == EntityDamageEvent.DamageCause.FIRE
                || mostRecentDamage.cause == EntityDamageEvent.DamageCause.FIRE_TICK ->
                EliminationCause.FIRE_DAMAGE

            mostRecentDamage.cause == EntityDamageEvent.DamageCause.SUFFOCATION ->
                EliminationCause.SUFFOCATION

            mostRecentDamage.cause == EntityDamageEvent.DamageCause.POISON ->
                EliminationCause.POISON

            mostRecentDamage.cause == EntityDamageEvent.DamageCause.WITHER ->
                EliminationCause.WITHER

            mostRecentDamage.cause == EntityDamageEvent.DamageCause.MAGIC ->
                EliminationCause.MAGIC

            mostRecentDamage.cause == EntityDamageEvent.DamageCause.THORNS ->
                EliminationCause.THORNS

            mostRecentDamage.cause == EntityDamageEvent.DamageCause.LIGHTNING ->
                EliminationCause.LIGHTNING

            mostRecentDamage.projectileType != null && mostRecentDamage.damager != null ->
                EliminationCause.PROJECTILE_BY_MOB

            else -> EliminationCause.UNKNOWN
        }
    }

    /**
     * Finds the most relevant player context for the elimination cause
     */
    private fun findContextPlayerForCause(
        cause: EliminationCause,
        entries: List<DamageEntry>
    ): Pair<EliminationCause, UUID?>
    {
        if (!cause.canHavePlayerContext)
        {
            return Pair(cause, null)
        }

        // Look for recent player damage that could provide context
        val playerDamage = entries
            .filter { it.damager != null && it.isRecentlyDamaged(RECENT_DAMAGE_MS) }
            .maxByOrNull { it.timestamp }

        return Pair(cause, playerDamage?.damager)
    }

    /**
     * Extracts damager information from damage event
     */
    private fun extractDamagerInfo(event: EntityDamageByEntityEvent): Pair<UUID, String>?
    {
        val damager = when (val entity = event.damager)
        {
            is Player -> entity
            is Projectile ->
            {
                val shooter = entity.shooter
                shooter as? Player
            }

            is TNTPrimed ->
            {
                // Check if TNT was placed/ignited by a player (would need additional tracking)
                entity.source
            }

            else -> null
        }

        return damager?.let { Pair(it.uniqueId, it.name) }
    }

    /**
     * Analyzes environmental context around the damage
     */
    private fun analyzeEnvironmentalContext(
        player: Player,
        event: EntityDamageEvent
    ): EnvironmentalContext
    {
        val location = player.location
        val nearbyBlocks = getNearbyBlockTypes(location)
        val wasInCombat = combatTags.containsKey(player.uniqueId) &&
            System.currentTimeMillis() - combatTags[player.uniqueId]!! <= COMBAT_TAG_MS

        return EnvironmentalContext(
            nearbyBlocks = nearbyBlocks,
            wasMovingFast = player.velocity.length() > 0.5,
            wasInCombat = wasInCombat,
            wasNearVoid = location.y <= VOID_Y_LEVEL + 5,
            wasNearLava = isNearLava(location)
        )
    }

    /**
     * Gets nearby block types for context
     */
    private fun getNearbyBlockTypes(location: org.bukkit.Location): Set<Material>
    {
        val blocks = mutableSetOf<Material>()
        val world = location.world ?: return blocks

        for (x in -2..2)
        {
            for (y in -2..2)
            {
                for (z in -2..2)
                {
                    val block = world.getBlockAt(
                        location.blockX + x,
                        location.blockY + y,
                        location.blockZ + z
                    )
                    blocks.add(block.type)
                }
            }
        }

        return blocks
    }

    /**
     * Checks if location is near lava
     */
    private fun isNearLava(location: org.bukkit.Location): Boolean
    {
        val world = location.world ?: return false

        for (x in -2..2)
        {
            for (y in -1..1)
            {
                for (z in -2..2)
                {
                    val block = world.getBlockAt(
                        location.blockX + x,
                        location.blockY + y,
                        location.blockZ + z
                    )
                    if (block.type == XMaterial.LAVA.parseMaterial())
                    {
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * Checks if location is near fire
     */
    private fun isNearFire(location: org.bukkit.Location): Boolean
    {
        val world = location.world ?: return false

        for (x in -2..2)
        {
            for (y in -1..1)
            {
                for (z in -2..2)
                {
                    val block = world.getBlockAt(
                        location.blockX + x,
                        location.blockY + y,
                        location.blockZ + z
                    )
                    if (block.type == XMaterial.FIRE.parseMaterial())
                    {
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * Extracts weapon used from damage event
     */
    private fun extractWeapon(event: EntityDamageEvent): Material?
    {
        if (event is EntityDamageByEntityEvent)
        {
            val damager = event.damager
            if (damager is Player)
            {
                return damager.itemInHand?.type
            }
        }
        return null
    }

    /**
     * Extracts projectile type from damage event
     */
    private fun extractProjectileType(event: EntityDamageEvent): Class<out Projectile>?
    {
        if (event is EntityDamageByEntityEvent)
        {
            val entity = event.damager
            if (entity is Projectile)
            {
                return entity.javaClass
            }
        }
        return null
    }

    /**
     * Cleans up expired damage entries for a specific player
     */
    private fun cleanupPlayerEntries(playerUUID: UUID)
    {
        damageHistory[playerUUID]?.removeIf { it.isExpired(DAMAGE_TIMEOUT_MS) }
        if (damageHistory[playerUUID]?.isEmpty() == true)
        {
            damageHistory.remove(playerUUID)
        }
    }

    /**
     * Cleans up all expired entries
     */
    private fun cleanupExpiredEntries()
    {
        val currentTime = System.currentTimeMillis()

        // Clean damage history
        damageHistory.forEach { (playerUUID, entries) ->
            entries.removeIf { it.isExpired(DAMAGE_TIMEOUT_MS) }
            if (entries.isEmpty())
            {
                damageHistory.remove(playerUUID)
            }
        }

        // Clean combat tags
        combatTags.entries.removeIf { (_, timestamp) ->
            currentTime - timestamp > COMBAT_TAG_MS
        }
    }

    /**
     * Clears all data for a player (use when they leave)
     */
    fun clearPlayer(playerUUID: UUID)
    {
        damageHistory.remove(playerUUID)
        combatTags.remove(playerUUID)
    }

    /**
     * Gets damage history for a player
     */
    fun getDamageHistory(playerUUID: UUID): List<DamageEntry>
    {
        return damageHistory[playerUUID]?.toList() ?: emptyList()
    }

    /**
     * Checks if player is in combat
     */
    fun isInCombat(playerUUID: UUID): Boolean
    {
        val lastCombat = combatTags[playerUUID] ?: return false
        return System.currentTimeMillis() - lastCombat <= COMBAT_TAG_MS
    }
}
