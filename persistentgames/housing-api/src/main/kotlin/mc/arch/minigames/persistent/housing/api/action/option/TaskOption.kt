package mc.arch.minigames.persistent.housing.api.action.option

import mc.arch.minigames.persistent.housing.api.action.util.HousingActionPrimitive

data class TaskOption(
    val name: String,
    var data: String,
    val primitive: HousingActionPrimitive,
    val id: String = name.lowercase()
)
