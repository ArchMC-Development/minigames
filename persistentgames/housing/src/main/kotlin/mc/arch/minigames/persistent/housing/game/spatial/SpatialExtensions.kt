package mc.arch.minigames.persistent.housing.game.spatial

import mc.arch.minigames.persistent.housing.api.spatial.WorldPosition
import org.bukkit.Location
import org.bukkit.World

fun WorldPosition.toLocation(world: World): Location = Location(world, this.x, this.y, this.z, this.yaw, this.pitch)

fun Location.toWorldPosition(): WorldPosition = WorldPosition(this.x, this.y, this.z, this.yaw, this.pitch)