package gg.tropic.practice

import gg.scala.commons.agnostic.sync.ServerSync
import gg.scala.commons.playerstatus.PlayerStatusTrackerService
import gg.scala.lemon.util.QuickAccess.username
import net.evilblock.cubed.util.CC
import java.util.UUID

/**
 * @author GrowlyX
 * @since 2/10/2024
 */
var devProvider: () -> Boolean = { false }

fun isDev() = devProvider()
fun isProd() = !devProvider()

fun namespace() = "tropicpractice"
fun namespaceShortened() = "tp"

fun practiceGroup() = "mip"
fun gameGroup() = "mipgame"
fun lobbyGroup() = "miplobby"

fun String.suffixWhenDev() = (if (isDev()) "${if (this == "tropicpractice")
    "tropicprac" else this}dev" else this)

fun isMiniGameServer() = "mipgame" in ServerSync.local.groups

fun UUID.toDisplayName() = PlayerStatusTrackerService.loadStatusOf(this)
    .join()
    ?.prefixedName
    ?: "${CC.GRAY}${username()}"
