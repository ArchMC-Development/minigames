package gg.solara.practice.editor

/**
 * @author GrowlyX
 * @since 7/18/2024
 */
import gg.scala.commons.spatial.Bounds
import gg.scala.commons.spatial.Position
import org.bukkit.Chunk

/**
 * Finds the northeast and southwest most locations from a List of world
 * Chunks. Allows us to find the bounds in which we will scan map
 * entities without the user having to manually fly around and mark them.
 */
fun List<Chunk>.toBounds(): Bounds
{
    if (isEmpty())
    {
        throw IllegalArgumentException("Chunk list cannot be empty")
    }

    var northeastMost: Position? = null
    var southwestMost: Position? = null

    for (chunk in this)
    {
        val chunkX = chunk.x
        val chunkZ = chunk.z

        val northeastX = chunkX * 16 + 15
        val northeastZ = chunkZ * 16 + 15

        val southwestX = chunkX * 16
        val southwestZ = chunkZ * 16

        val northeastLocation = Position(northeastX.toDouble(), 0.0, northeastZ.toDouble())
        val southwestLocation = Position(southwestX.toDouble(), 0.0, southwestZ.toDouble())

        if (northeastMost == null)
        {
            northeastMost = northeastLocation
        } else
        {
            if (
                northeastLocation.x > northeastMost.x ||
                (northeastLocation.x == northeastMost.x && northeastLocation.z > northeastMost.z)
            )
            {
                northeastMost = northeastLocation
            }
        }

        if (southwestMost == null)
        {
            southwestMost = southwestLocation
        } else
        {
            if (
                southwestLocation.x < southwestMost.x ||
                (southwestLocation.x == southwestMost.x && southwestLocation.z < southwestMost.z)
            )
            {
                southwestMost = southwestLocation
            }
        }
    }

    return Bounds(northeastMost!!, southwestMost!!)
}

