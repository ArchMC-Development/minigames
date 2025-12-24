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
    "playerQuitEvent" to ActionDisplayBundle(
        "Player Quit Event",
        XMaterial.SKELETON_SKULL
    ),
    "playerDeathEvent" to ActionDisplayBundle(
        "Player Death Event",
        XMaterial.DIAMOND_SWORD
    ),
    "playerDamageEvent" to ActionDisplayBundle(
        "Player Damage Event",
        XMaterial.LAVA_BUCKET
    ),
    "playerMoveEvent" to ActionDisplayBundle(
        "Player Move Event",
        XMaterial.IRON_BOOTS
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
    "broadcastMessage" to ActionDisplayBundle(
        "Send Message",
        XMaterial.OAK_SIGN
    ),
    "fullHeal" to ActionDisplayBundle(
        "Full Heal",
        XMaterial.GOLDEN_APPLE
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