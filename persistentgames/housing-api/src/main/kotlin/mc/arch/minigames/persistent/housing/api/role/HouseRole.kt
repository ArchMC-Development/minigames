package mc.arch.minigames.persistent.housing.api.role

data class HouseRole(
    val name: String,
    var displayName: String = name,
    val permissions: MutableList<String> = mutableListOf(),
    var color: String = "&7",
    var prefix: String = color,
    var default: Boolean = false,
    val id: String = name,
)
{
    companion object
    {
        fun defaults() = mutableMapOf(
            "guest" to HouseRole(
                name = "guest",
                displayName = "Guest",
                permissions = mutableListOf(),
                color = "&7",
                prefix = "&7[GUEST]",
                default = true,
            ),
            "resident" to HouseRole(
                name = "resident",
                displayName = "Resident",
                prefix = "&6[RESIDENT]",
                permissions = mutableListOf("house.interact"),
                color = "&6",
            ),
            "co-owner" to HouseRole(
                name = "co-owner",
                displayName = "Co-Owner",
                permissions = mutableListOf("house.manage"),
                color = "&e",
                prefix = "&e[CO-OWNER]",
            ),
            "owner" to HouseRole(
                name = "owner",
                displayName = "Owner",
                permissions = mutableListOf("house.manage"),
                color = "&c",
                prefix = "&c[OWNER]"
            ),
        )
    }

    fun coloredName() = "$color$displayName"
}