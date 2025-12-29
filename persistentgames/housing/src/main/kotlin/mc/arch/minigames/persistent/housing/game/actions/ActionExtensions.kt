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
    "playerDamageByPlayerEvent" to ActionDisplayBundle(
        "Player Damage By Player Event",
        XMaterial.GOLDEN_SWORD
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
    ),
    "playerInteractEvent" to ActionDisplayBundle(
        "Player Interact Event",
        XMaterial.STICK
    ),
    "playerInteractEntityEvent" to ActionDisplayBundle(
        "Player Interact Entity Event",
        XMaterial.VILLAGER_SPAWN_EGG
    ),
    "playerRespawnEvent" to ActionDisplayBundle(
        "Player Respawn Event",
        XMaterial.BEACON
    ),
    "playerTeleportEvent" to ActionDisplayBundle(
        "Player Teleport Event",
        XMaterial.ENDER_PEARL
    ),
    "playerDropItemEvent" to ActionDisplayBundle(
        "Player Drop Item Event",
        XMaterial.DROPPER
    ),
    "playerPickupItemEvent" to ActionDisplayBundle(
        "Player Pickup Item Event",
        XMaterial.HOPPER
    ),
    "playerChatEvent" to ActionDisplayBundle(
        "Player Chat Event",
        XMaterial.PAPER
    ),
    "playerCommandEvent" to ActionDisplayBundle(
        "Player Command Event",
        XMaterial.COMMAND_BLOCK
    ),
    "playerSneakEvent" to ActionDisplayBundle(
        "Player Sneak Event",
        XMaterial.LEATHER_BOOTS
    ),
    "playerSprintEvent" to ActionDisplayBundle(
        "Player Sprint Event",
        XMaterial.GOLDEN_BOOTS
    ),
    "playerFishEvent" to ActionDisplayBundle(
        "Player Fish Event",
        XMaterial.FISHING_ROD
    ),
    "playerProjectileEvent" to ActionDisplayBundle(
        "Player Projectile Launch Event",
        XMaterial.BOW
    ),
    "playerFoodChangeEvent" to ActionDisplayBundle(
        "Player Food Change Event",
        XMaterial.COOKED_BEEF
    )
)

val taskBundles = mutableMapOf(
    "sendMessage" to ActionDisplayBundle(
        "Send Message",
        XMaterial.PAPER
    ),
    "broadcastMessage" to ActionDisplayBundle(
        "Broadcast Message",
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

fun Task.asRegistered(): Task = HousingActionBukkitImplementation.getAllTasks().first { it.id == this.id }

fun Task.getDisplayBundle(): ActionDisplayBundle = taskBundles[this.id]!!
fun ActionEvent.getDisplayBundle(): ActionDisplayBundle = bundles[this.id()]!!