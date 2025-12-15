package mc.arch.minigames.persistent.housing.api.role

data class HouseRole(
    val name: String,
    val displayName: String,
    val permissions: MutableList<RolePermissionLevel> = mutableListOf(),
    val color: String = "&7",
    val prefix: String = color,
    val id: String = name,
)