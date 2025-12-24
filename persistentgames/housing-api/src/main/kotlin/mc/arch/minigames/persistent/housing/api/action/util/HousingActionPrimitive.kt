package mc.arch.minigames.persistent.housing.api.action.util

/**
 * Class created on 12/23/2025

 * @author Max C.
 * @project arch-minigames
 * @website https://solo.to/redis
 */
enum class HousingActionPrimitive(
    val mutator: (String) -> String?,
    val errorMessage: String,
)
{
    BOOLEAN({ input ->
        input.toBoolean().toString()
    }, "Please enter either True or False for this option!"),

    INTEGER({ input ->
        input.toIntOrNull()?.toString()
    }, "Please enter a valid Number for this option!"),

    STRING({ input ->
        input
    }, "Please enter a valid String for this option!"),
}