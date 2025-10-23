package gg.tropic.practice.extensions

import org.bukkit.Location

const val hypixelSpectatorCylinderRadius = 13
const val hypixelSpectatorCylinderHeight = 11

fun getCylinderBlocks(center: Location, radius: Int, height: Int): List<Location>
{
    val blocks = mutableListOf<Location>()
    val world = center.world ?: return blocks

    val cx = center.blockX
    val cy = center.blockY
    val cz = center.blockZ

    for (y in 0 until height)
    {
        for (x in -radius..radius)
        {
            for (z in -radius..radius)
            {
                if (x * x + z * z <= radius * radius)
                {
                    blocks.add(Location(world, (cx + x).toDouble(), (cy + y).toDouble(), (cz + z).toDouble()))
                }
            }
        }
    }

    return blocks
}
