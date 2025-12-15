package mc.arch.minigames.persistent.housing.api.entity

import mc.arch.minigames.persistent.housing.api.content.HousingItemStack

data class HousingHologram(
    val name: String,
    val id: String = name.lowercase(),
    val lines: MutableList<String> = mutableListOf(),
    val floatingItem: HousingItemStack? = null
)
