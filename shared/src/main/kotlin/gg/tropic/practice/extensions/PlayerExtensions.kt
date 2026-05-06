package gg.tropic.practice.extensions

import gg.tropic.practice.versioned.Versioned
import me.lucko.helper.Helper
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue

/**
 * @author Subham
 * @since 7/23/25
 */
fun Player.mountImmovable()
{
    val mountId = Versioned.toProvider().getPlayerProvider().mountImmovable(this)
    setMetadata("seated", FixedMetadataValue(Helper.hostPlugin(), mountId))
}

fun Player.mountImmovable(location: Location)
{
    val mountId = Versioned.toProvider().getPlayerProvider().mountImmovable(this, location)
    setMetadata("seated", FixedMetadataValue(Helper.hostPlugin(), mountId))
}

fun Player.unmount()
{
    if (hasMetadata("seated"))
    {
        val mountId = getMetadata("seated").firstOrNull()?.asInt() ?: return
        Versioned.toProvider().getPlayerProvider().unmount(this, mountId)
        removeMetadata("seated", Helper.hostPlugin())
    }
}

fun Player.addOrDrop(vararg itemStack: org.bukkit.inventory.ItemStack)
{
    val added = inventory.addItem(*itemStack)
    added.forEach {
        world.dropItemNaturally(location, it.value)
    }
}
