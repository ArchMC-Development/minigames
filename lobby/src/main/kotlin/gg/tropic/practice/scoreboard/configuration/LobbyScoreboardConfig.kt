package gg.tropic.practice.scoreboard.configuration

import gg.scala.commons.graduation.Progressive
import gg.tropic.practice.scoreboard.animation.AnimationFrame
import net.evilblock.cubed.util.CC

/**
 * Class created on 10/12/2025

 * @author Max C.
 * @project arch-minigames
 * @website https://solo.to/redis
 */
class LobbyScoreboardConfig(
    var title: String = "${CC.B_PRI}${CC.I}ARCH${CC.B_GRAY}${CC.I}.${CC.B_WHITE}${CC.I}MC",
    var titleAnimated: Boolean = true,
    var titleFrames: List<AnimationFrame> = arrayListOf(
        AnimationFrame("&4&lARCH&7&l.&f&lMC", 6000),
        AnimationFrame("&f&lA", 550),
        AnimationFrame("&4&lA&f&lR", 550),
        AnimationFrame("&4&lAR&f&lC", 550),
        AnimationFrame("&4&lARC&f&lH", 550),
        AnimationFrame("&4&lARCH&7&l.", 550),
        AnimationFrame("&4&lARCH&7&l.&f&lM", 550),
        AnimationFrame("&4&lARCH&7&l.&f&lMC", 6000),
    ),
    var primaryColor: String = "${CC.PRI}",
    var secondaryColor: String = "${CC.B_WHITE}",
    override var matured: Set<String>? = setOf()
) : Progressive