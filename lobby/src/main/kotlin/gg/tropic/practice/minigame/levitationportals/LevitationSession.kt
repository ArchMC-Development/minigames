package gg.tropic.practice.minigame.levitationportals

import me.lucko.helper.terminable.composite.CompositeTerminable
import org.bukkit.Location

/**
 * @author Subham
 * @since 10/23/25
 */
data class LevitationSession(
    val portalId: String,
    val location: Location,
    var incremental: Int,
    val terminable: CompositeTerminable
)
