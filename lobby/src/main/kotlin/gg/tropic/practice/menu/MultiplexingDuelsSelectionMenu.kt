package gg.tropic.practice.menu

import com.cryptomorin.xseries.XMaterial
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player

class MultiplexingDuelsSelectionMenu(
    private val onSelect: (Boolean) -> Unit
) : Menu("Select Format")
{
    init
    {
        placeholder = true
    }

    override fun size(buttons: Map<Int, Button>) = 27

    override fun getButtons(player: Player) = mutableMapOf(
        11 to ItemBuilder
            .of(XMaterial.IRON_SWORD)
            .name("${CC.GREEN}Legacy")
            .addToLore(
                "${CC.GRAY}Play with classic kits",
                "${CC.GRAY}built for legacy combat.",
                "",
                "${CC.GREEN}Click to play!"
            )
            .toButton { _, _ ->
                Button.playNeutral(player)
                onSelect(false)
            },
        15 to ItemBuilder
            .of(XMaterial.END_CRYSTAL)
            .name("${CC.AQUA}Modern")
            .addToLore(
                "${CC.GRAY}Play with new kits",
                "${CC.GRAY}built for modern combat.",
                "",
                "${CC.GREEN}Click to play!"
            )
            .toButton { _, _ ->
                Button.playNeutral(player)
                onSelect(true)
            }
    )
}
