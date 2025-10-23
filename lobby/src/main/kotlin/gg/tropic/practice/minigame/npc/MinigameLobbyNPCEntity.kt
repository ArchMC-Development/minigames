package gg.tropic.practice.minigame.npc

import gg.tropic.practice.configuration.minigame.MinigameLobbyNPC
import gg.tropic.practice.replacements.toTemplatePlayerCounts
import net.evilblock.cubed.entity.EntityHandler
import net.evilblock.cubed.entity.npc.NpcEntity
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 6/29/25
 */
class MinigameLobbyNPCEntity(
    val configuration: MinigameLobbyNPC,
) : NpcEntity(
    lines = listOf(
        "${CC.B_YELLOW}${configuration.actionLabel}",
        "${CC.RED}${configuration.gamemodeName}",
        "${CC.B_YELLOW}0 playing"
    ),
    location = configuration.position.toLocation(
        Bukkit.getWorlds().first()
    )
)
{
    init
    {
        persistent = false
    }

    private var isBlueNewlyReleased = false
    fun generateLines() = if (configuration.newlyReleased)
    {
        isBlueNewlyReleased = !isBlueNewlyReleased
        listOf(
            "${if (isBlueNewlyReleased) CC.B_AQUA else CC.B_WHITE}${
                configuration.broadcastLabel
            }",
            "${CC.B_YELLOW}${configuration.actionLabel}",
            "${CC.RED}${configuration.gamemodeName}",
            "${CC.B_YELLOW}${configuration.replacement.toTemplatePlayerCounts()}"
        )
    } else
    {
        listOf(
            "${CC.B_YELLOW}${configuration.actionLabel}",
            "${CC.RED}${configuration.gamemodeName}",
            "${CC.B_YELLOW}${configuration.replacement.toTemplatePlayerCounts()}"
        )
    }

    fun configure()
    {
        initializeData()
        EntityHandler.trackEntity(this)

        updateLines(generateLines())
        updateTexture(
            configuration.skinType.value,
            configuration.skinType.signature
        )
        updateItemInHand(configuration.heldItem)

        updateForCurrentWatchers()
    }

    override fun onRightClick(player: Player)
    {
        player.performCommand(configuration.command)
    }

    override fun onLeftClick(player: Player)
    {
        player.performCommand(configuration.command)
    }
}
