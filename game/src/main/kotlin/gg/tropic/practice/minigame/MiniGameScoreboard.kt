package gg.tropic.practice.minigame

import gg.scala.commons.agnostic.sync.ServerSync.getLocalGameServer
import gg.scala.lemon.LemonConstants
import gg.tropic.practice.games.GameReport
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import org.bukkit.entity.Player
import java.text.SimpleDateFormat
import java.util.Date

/**
 * @author GrowlyX
 * @since 8/19/2024
 */
val dateFormat = SimpleDateFormat("MM/dd/yy")

interface MiniGameScoreboard
{
    val game: AbstractMiniGameGameImpl<*>

    fun titleFor(player: Player): String

    fun createWaitingScoreboardFor(player: Player, board: MutableList<String>)
    fun createStartingScoreboardFor(player: Player, board: MutableList<String>)
    fun createInGameScoreboardFor(player: Player, board: MutableList<String>)
    fun createEndingScoreboardFor(player: Player, report: GameReport?, board: MutableList<String>)

    fun MutableList<String>.surround(extra: (MutableList<String>) -> Unit)
    {
        this += "${CC.GRAY}${dateFormat.format(Date())} ${CC.D_GRAY}${getLocalGameServer()
            .id
            .split("-")
            .lastOrNull() ?: "??"}"
        this += ""

        val extras = mutableListOf<String>()
        extra(extras)

        this += extras
    }
}
