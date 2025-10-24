package gg.tropic.practice.visuals

import gg.tropic.practice.games.team.TeamIdentifier
import org.bukkit.ChatColor
import org.bukkit.Color

/**
 * @author GrowlyX
 * @since 5/26/2022
 */
object TeamVisuals
{
    private val chatColorToReadable = mapOf(
        ChatColor.LIGHT_PURPLE to "Pink"
    )

    private val chatColorToArmorColor = mapOf(
        ChatColor.RED to Color.RED,
        ChatColor.BLUE to Color.BLUE,
        ChatColor.GREEN to Color.LIME,
        ChatColor.YELLOW to Color.YELLOW,
        ChatColor.AQUA to Color.AQUA,
        ChatColor.WHITE to Color.WHITE,
        ChatColor.LIGHT_PURPLE to Color.FUCHSIA,
        ChatColor.GRAY to Color.GRAY
    )

    fun toArmorColor(chatColor: ChatColor) = chatColorToArmorColor[chatColor] ?: Color.WHITE

    fun toChatColor(teamIdentifier: TeamIdentifier) = when (teamIdentifier.label)
    {
        "A" -> ChatColor.RED
        "B" -> ChatColor.BLUE
        "C" -> ChatColor.GREEN
        "D" -> ChatColor.YELLOW
        "E" -> ChatColor.AQUA
        "F" -> ChatColor.WHITE
        "G" -> ChatColor.LIGHT_PURPLE
        "H" -> ChatColor.GRAY
        else -> ChatColor.BLACK
    }

    fun toReadable(color: ChatColor) = chatColorToReadable[color]
        ?: color.name.lowercase().capitalize()
}
