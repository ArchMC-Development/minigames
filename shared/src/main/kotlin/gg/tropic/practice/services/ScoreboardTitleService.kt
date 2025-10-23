package gg.tropic.practice.services

import gg.scala.commons.agnostic.sync.ServerSync
import gg.scala.commons.scoreboard.TextAnimator
import gg.scala.commons.scoreboard.animations.TextFadeAnimation
import gg.scala.flavor.service.Close
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import net.evilblock.cubed.util.CC
import org.bukkit.ChatColor

/**
 * @author GrowlyX
 * @since 1/19/2024
 */
@Service
object ScoreboardTitleService
{
    private val titleAnimator = TextAnimator.of(
        TextFadeAnimation(
            "${CC.BOLD}Duels",
            ChatColor.YELLOW,
            ChatColor.DARK_GRAY,
            ChatColor.GREEN
        )
    )

    fun getCurrentTitle() = "${titleAnimator.current().text}${
        if ("mipdev" in ServerSync.getLocalGameServer().groups) " ${CC.WHITE}(dev)" else ""
    }"

    @Configure
    fun configure()
    {
        titleAnimator.schedule()
    }

    @Close
    fun close()
    {
        titleAnimator.dispose()
    }
}
