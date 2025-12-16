package mc.arch.minigames.persistent.housing.api.role

data class HouseRole(
    val name: String,
    val displayName: String = name,
    val permissions: MutableList<String> = mutableListOf(),
    val color: String = "&7",
    val prefix: String = color,
    var default: Boolean = false,
    val id: String = name,
)