package gg.tropic.practice.map.administration.specific

import com.cryptomorin.xseries.XMaterial
import gg.tropic.practice.map.MapService
import gg.tropic.practice.map.administration.MapAdminMenu
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 7/28/2024
 */
class SpecificMapAdminMenu(private val map: gg.tropic.practice.map.Map) : Menu(
    "Editing ${map.name}..."
)
{
    override fun getButtons(player: Player) = mapOf(
        1 to ItemBuilder
            .of(XMaterial.NETHER_STAR)
            .name("${CC.RED}Lock")
            .addToLore(
                "${CC.GRAY}Is the map locked?",
                if (map.locked) "${CC.GREEN}Yes" else "${CC.RED}No"
            )
            .toButton { _, _ ->
                Button.playSuccess(player)
                with(MapService.cached()) {
                    map.locked = !map.locked
                    MapService.sync(this)
                }
            }
    )

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose)
        {
            Tasks.delayed(1L) {
                MapAdminMenu().openMenu(player)
            }
        }
    }
}
