package mc.arch.minigames.persistent.housing.api.role

/**
 * Class created on 12/29/2025

 * @author Max C.
 * @project arch-minigames
 * @website https://solo.to/redis
 */
enum class HousePermission(val displayName: String, val description: List<String>, val node: String)
{
    MANAGE(
        "Manage",
        listOf(
            "Administrative access to this house.",
            "Can do everything except delete it.",
        ),
        "house.manage"
    ),
    INTERACT(
        "Interact",
        listOf(
            "Allows users to interact with parts of",
            "the house. Things such as open chests,",
            "step on pressure plates, click buttons,",
            "etc"
        ),
        "house.interact"
    );

    fun fromNode(node: String) = entries.firstOrNull { it.node == node }
}