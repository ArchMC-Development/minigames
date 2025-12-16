package mc.arch.minigames.persistent.housing.api.action.option

data class TaskOption(
    val name: String,
    var data: String,
    val id: String = name.lowercase()
)
