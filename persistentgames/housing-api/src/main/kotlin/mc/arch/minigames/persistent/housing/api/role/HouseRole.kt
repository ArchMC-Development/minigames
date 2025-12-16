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
{
    companion object
    {
        fun defaults() = mutableMapOf(
            "guest" to HouseRole(
                name = "guest",
                permissions = mutableListOf(),
                color = "&7",
                default = true,
            ),
            "resident" to HouseRole(
                name = "resident",
                permissions = mutableListOf("house.interact"),
                color = "&6",
            ),
            "co-owner" to HouseRole(
                name = "co-owner",
                permissions = mutableListOf("house.manage"),
                color = "&e",
            ),
            "owner" to HouseRole(
                name = "owner",
                permissions = mutableListOf("house.manage"),
                color = "&c",
            ),
        )
    }
}