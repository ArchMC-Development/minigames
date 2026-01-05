package mc.arch.minigames.persistent.housing.api.entity

import mc.arch.minigames.persistent.housing.api.content.HousingItemStack
import mc.arch.minigames.persistent.housing.api.spatial.WorldPosition

data class HousingHologram(
    val name: String,
    var location: WorldPosition,
    val id: String = name.lowercase(),
    var lines: MutableList<String> = mutableListOf(name),
    val floatingItem: HousingItemStack? = null
)
