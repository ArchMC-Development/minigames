package mc.arch.minigames.persistent.housing.api.content

/**
 * This is a little bit autistic, but for
 * base item storage for stuff like floating
 * items, it should be fine.
 */
data class HousingItemStack(
    val material: String,
    val displayName: String = material,
    val description: MutableList<String> = mutableListOf(),
    val data: Short = 0,
    val amount: Int = 1,
)