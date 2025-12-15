package mc.arch.minigames.persistent.housing.game.spatial

import mc.arch.minigames.persistent.housing.api.spatial.WorldPosition
import org.bukkit.Bukkit
import org.bukkit.Location

fun WorldPosition.toLocation(): Location = Location(Bukkit.getWorld(this.world), this.x, this.y, this.z)