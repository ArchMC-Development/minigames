package mc.arch.minigames.persistent.housing.api.entity

import mc.arch.minigames.persistent.housing.api.spatial.WorldPosition

/**
 * This should be a 1:1 translation
 * of the actual NPC entity model
 * that is in commons
 *
 * On the game side of this, we can just
 * have some extension function to translate
 * the house NPC to bukkit entity.
 *
 * Also see [HousingHologram]
 */
data class HousingNPC(
    val name: String,
    var location: WorldPosition,
    val id: String = name.lowercase(),
    var displayName: String = name,
    var command: String? = null,
    val aboveHeadText: MutableList<String> = mutableListOf(name),
    val messagesToSend: MutableList<String> = mutableListOf(),
    val skinTexture: String? = null,
    val skinSignature: String? = null,
    var glowing: Boolean = false
)