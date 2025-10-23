package gg.solara.practice.editor

import com.cryptomorin.xseries.XMaterial
import org.bukkit.World
import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 6/24/25
 */
interface Editable
{
    val icon: XMaterial
    val displayName: String

    fun prepareWorld(player: Player): World

    fun isComplete() = EditorStatusMarker.isComplete(displayName)
    fun markComplete() = EditorStatusMarker.markAsComplete(displayName)
    fun markInComplete() = EditorStatusMarker.markAsInComplete(displayName)
}
