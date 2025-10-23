package gg.tropic.practice.commands.menu.admin.config

import com.cryptomorin.xseries.XMaterial
import gg.tropic.practice.configuration.PracticeConfigurationService
import gg.scala.commons.spatial.Position
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import org.bukkit.entity.Player

/**
 * Class created on 1/17/2025

 * @author Max C.
 * @project esta-practice
 * @website https://solo.to/redis
 */
class PracticeConfigurationMenu : Menu()
{
    init
    {
        placeholder = true
        updateAfterClick = true
    }

    val config = PracticeConfigurationService.cached()

    override fun getButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf<Int, Button>().also {
            it[0] = ItemBuilder.of(XMaterial.EMERALD)
                .name("${CC.B_GREEN}Ranked Play")
                .addToLore(
                    "${CC.GRAY}Change whether or not ranked play",
                    "${CC.GRAY}is supported on this server.",
                    "${CC.GRAY}Currently: ${if (config.rankedQueueEnabled) "${CC.GREEN}Enabled" else "${CC.RED}Disabled"}",
                    "",
                    if (config.rankedQueueEnabled) "${CC.RED}Click to disable" else "${CC.GREEN}Click to enable"
                ).toButton { _, _ ->
                    config.rankedQueueEnabled = !config.rankedQueueEnabled
                    PracticeConfigurationService.sync(config)
                }
        }
    }

    override fun getTitle(player: Player): String
    {
        return "Select a Setting"
    }

    override fun size(buttons: Map<Int, Button>): Int = 9
}
