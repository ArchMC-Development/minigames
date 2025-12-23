package mc.arch.minigames.persistent.housing.game.actions

import com.cryptomorin.xseries.XMaterial
import mc.arch.minigames.persistent.housing.api.action.player.ActionEvent
import mc.arch.minigames.persistent.housing.api.action.tasks.Task
import mc.arch.minigames.persistent.housing.game.actions.display.ActionDisplayBundle

//misc things that may be useful
val bundles = mutableMapOf(
    "playerJoinEvent" to ActionDisplayBundle(
        "Player Join Event",
        XMaterial.PLAYER_HEAD
    ),
    "blockBreakEvent" to ActionDisplayBundle(
        "Block Break Event",
        XMaterial.DIRT
    ),
    "blockPlaceEvent" to ActionDisplayBundle(
        "Block Place Event",
        XMaterial.GRASS_BLOCK
    )
)

val taskBundles = mutableMapOf(
    "sendMessage" to ActionDisplayBundle(
        "Send Message",
        XMaterial.PAPER
    ),
    "preventBlockPlace" to ActionDisplayBundle(
        "Prevent Block Place",
        XMaterial.BARRIER
    ),
    "preventBlockBreak" to ActionDisplayBundle(
        "Prevent Block Break",
        XMaterial.BARRIER
    )
)

fun Task.getDisplayBundle(): ActionDisplayBundle = taskBundles[this.id]!!
fun ActionEvent.getDisplayBundle(): ActionDisplayBundle = bundles[this.id()]!!