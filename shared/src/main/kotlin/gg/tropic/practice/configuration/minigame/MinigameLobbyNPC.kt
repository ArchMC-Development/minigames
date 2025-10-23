package gg.tropic.practice.configuration.minigame

import com.cryptomorin.xseries.XMaterial
import gg.scala.commons.spatial.Position
import gg.tropic.practice.configuration.minigame.type.LobbyNPCSkinType
import org.bukkit.inventory.ItemStack

/**
 * @author Subham
 * @since 6/24/25
 */
data class MinigameLobbyNPC(
    var heldItem: ItemStack = XMaterial.RED_BED.parseItem()!!,
    var command: String = "joinqueue bedwars",
    var gamemodeName: String = "BedWars",
    var replacement: String = "<minigame_bwlobby_bedwars>",
    var broadcastLabel: String = "NEWLY RELEASED",
    var actionLabel: String = "CLICK TO PLAY",
    var skinType: LobbyNPCSkinType = LobbyNPCSkinType.BEDWARS,
    var newlyReleased: Boolean = false,
    var position: Position = Position(0.0, 0.0, 0.0)
)
