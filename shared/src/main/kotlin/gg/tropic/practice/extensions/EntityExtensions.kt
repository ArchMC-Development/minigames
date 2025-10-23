package gg.tropic.practice.extensions

import gg.tropic.practice.games.team.TeamIdentifier
import me.lucko.helper.Helper
import net.evilblock.cubed.util.ServerVersion
import org.bukkit.GameMode
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.metadata.Metadatable

/**
 * @author GrowlyX
 * @since 8/5/2022
 */
fun Metadatable.ownedBy() = getMetadata("ownership").firstOrNull()
    ?.value()
    ?.let { it as TeamIdentifier }

fun Metadatable.setOwnership(id: TeamIdentifier) = setMetadata(
    "ownership",
    FixedMetadataValue(Helper.hostPlugin(), id)
)

fun Player.resetAttributes(editFlightAttributes: Boolean = true)
{
    this.health = this.maxHealth

    this.foodLevel = 20
    this.saturation = 12.8f
    this.maximumNoDamageTicks = 20
    this.fireTicks = 0
    this.fallDistance = 0.0f
    this.level = 0
    this.exp = 0.0f
    this.walkSpeed = 0.2f
    this.inventory.heldItemSlot = 0

    if (editFlightAttributes)
    {
        this.isFlying = false
        this.allowFlight = false
    }

    this.closeInventory()

    this.inventory.clear()
    this.inventory.armorContents = null

    this.gameMode = GameMode.SURVIVAL
    this.fireTicks = 0

    if (ServerVersion.getVersion().isOlderThan(ServerVersion.v1_19))
    {
        (this as CraftLivingEntity).handle.dataWatcher.watch<Byte>(9, -1)
    }

    for (potionEffect in this.activePotionEffects)
    {
        this.removePotionEffect(potionEffect.type)
    }

    this.updateInventory()
}
