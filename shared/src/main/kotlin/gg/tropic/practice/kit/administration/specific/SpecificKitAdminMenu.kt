package gg.tropic.practice.kit.administration.specific

import com.cryptomorin.xseries.XMaterial
import gg.tropic.practice.kit.Kit
import gg.tropic.practice.kit.KitService
import gg.tropic.practice.kit.administration.KitAdminMenu
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
class SpecificKitAdminMenu(private val kit: Kit) : Menu(
    "Editing ${kit.id}..."
)
{
    override fun getButtons(player: Player) = mapOf(
        1 to ItemBuilder
            .of(XMaterial.NETHER_STAR)
            .name("${CC.RED}Enabled")
            .addToLore(
                "${CC.GRAY}Is the kit enabled?",
                if (kit.enabled) "${CC.GREEN}Yes" else "${CC.RED}No"
            )
            .toButton { _, _ ->
                Button.playSuccess(player)
                kit.enabled = !kit.enabled

                with(KitService.cached()) {
                    KitService.cached().kits[kit.id] = kit
                    KitService.sync(this)

                    openMenu(player)
                }
            },
        5 to ItemBuilder
            .of(XMaterial.END_PORTAL_FRAME)
            .name("${CC.GREEN}Lifecycle")
            .addToLore(
                "${CC.WHITE}${
                    kit.lifecycleType?.toString() ?: "SoulBound"
                }"
            )
            .toButton(),
        3 to ItemBuilder
            .of(XMaterial.EXPERIENCE_BOTTLE)
            .name("${CC.RED}Features")
            .apply {
                if (kit.features.isEmpty())
                {
                    addToLore("${CC.RED}None")
                    return@apply
                }

                kit.features
                    .forEach { (flag, meta) ->
                        addToLore("${CC.WHITE}- ${CC.WHITE}${flag.name}")

                        if (meta.isNotEmpty())
                        {
                            addToLore("   ${CC.GRAY}Metadata:")
                            meta.forEach { (k, v) ->
                                addToLore("    ${CC.WHITE}$k: $v")
                            }
                        }
                    }

                addToLore("", "${CC.GREEN}Click to edit!")
            }
            .toButton { _, _ ->
                Button.playNeutral(player)
                KitFeaturesEditMenu(kit).openMenu(player)
            }
    )

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose)
        {
            Tasks.sync {
                KitAdminMenu(player).openMenu(player)
            }
        }
    }
}
