package mc.arch.minigames.persistent.housing.game.actions

import com.cryptomorin.xseries.XMaterial
import mc.arch.minigames.persistent.housing.api.action.player.ActionEvent
import mc.arch.minigames.persistent.housing.game.actions.display.ActionDisplayBundle

//misc things that may be useful
val bundles = mutableMapOf(
    "playerJoinEvent" to ActionDisplayBundle(
        "Player Join Event",
        XMaterial.PLAYER_HEAD
    ),
    "blockBreakEvent" to ActionDisplayBundle(
        "Block Break Event",
        XMaterial.GRASS_BLOCK
    )
)

fun ActionEvent.getDisplayBundle(): ActionDisplayBundle = bundles[this.id()]!!