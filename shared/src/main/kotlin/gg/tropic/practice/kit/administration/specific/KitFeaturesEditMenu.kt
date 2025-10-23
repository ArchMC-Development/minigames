package gg.tropic.practice.kit.administration.specific

import com.cryptomorin.xseries.XMaterial
import gg.tropic.practice.commands.menu.admin.kit.SpecificKitEditorMenu
import gg.tropic.practice.kit.Kit
import gg.tropic.practice.kit.KitService
import gg.tropic.practice.kit.feature.FeatureFlag
import gg.tropic.practice.kit.feature.GameLifecycle
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 7/28/2024
 */
class KitFeaturesEditMenu(private val kit: Kit) : PaginatedMenu()
{
    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()
        val lifecycle = kit.lifecycleType ?: GameLifecycle.SoulBound
        for (flag in FeatureFlag.entries)
        {
            val isIncompatible = flag.incompatibleWith().filter { kit.features(it) }
            val notApplicableYet = flag.requires.filter { !kit.features(it) }

            buttons[buttons.size] = ItemBuilder
                .of(when (true)
                {
                    (lifecycle == GameLifecycle.MiniGame && lifecycle !in flag.availableLifecycles) -> XMaterial.BLACK_STAINED_GLASS
                    (lifecycle !in flag.availableLifecycles) -> XMaterial.COAL_BLOCK
                    isIncompatible.isNotEmpty() -> XMaterial.RED_WOOL
                    notApplicableYet.isNotEmpty() -> XMaterial.LIGHT_GRAY_WOOL
                    (kit.features(flag)) -> XMaterial.LIME_WOOL
                    else -> XMaterial.GRAY_WOOL
                })
                .name("${CC.GREEN}${flag.name}")
                .apply {
                    val separatorBar = "${CC.STRIKE_THROUGH}------------------------"
                    if (lifecycle !in flag.availableLifecycles)
                    {
                        if (lifecycle == GameLifecycle.MiniGame)
                        {
                            addToLore(
                                "${CC.RED}$separatorBar",
                                "${CC.BD_GRAY}Deregulated Incompatible:",
                                "${CC.D_GRAY}$lifecycle not supported,",
                                "${CC.D_GRAY}but is deregulated",
                                "${CC.D_GRAY}$separatorBar"
                            )
                        } else
                        {
                            addToLore(
                                "${CC.RED}$separatorBar",
                                "${CC.B_RED}Incompatible:",
                                "${CC.RED}$lifecycle not supported!",
                                "${CC.RED}$separatorBar"
                            )
                        }
                    } else if (isIncompatible.isNotEmpty())
                    {
                        addToLore(
                            "${CC.GOLD}$separatorBar",
                            "${CC.B_GOLD}Incompatible Features:",
                            "${CC.GOLD}Remove the following features",
                            *isIncompatible
                                .map { "${CC.WHITE}- ${it.name}" }
                                .toTypedArray(),
                            "${CC.GOLD}$separatorBar"
                        )
                    } else if (notApplicableYet.isNotEmpty())
                    {
                        addToLore(
                            "${CC.PINK}$separatorBar",
                            "${CC.B_PINK}Required Features Missing:",
                            "${CC.PINK}Add the following features",
                            *notApplicableYet
                                .map { "${CC.WHITE}- ${it.name}" }
                                .toTypedArray(),
                            "${CC.PINK}$separatorBar"
                        )
                    } else if (kit.features(flag))
                    {
                        addToLore(
                            "${CC.GREEN}$separatorBar",
                            "${CC.GREEN}Enabled",
                            "${CC.GREEN}$separatorBar"
                        )
                    } else
                    {
                        addToLore(
                            "${CC.GRAY}$separatorBar",
                            "${CC.GRAY}Available",
                            "${CC.GRAY}$separatorBar"
                        )
                    }
                }
                .apply {
                    if (flag.description.isNotEmpty())
                    {
                        flag.description
                            .onEach {
                                addToLore("${CC.WHITE}$it")
                            }
                        addToLore("")
                    }
                }
                .apply {
                    if (flag.schema.isNotEmpty())
                    {
                        addToLore("${CC.GREEN}Metadata:")

                        flag.schema.forEach { (k, v) ->
                            val configured = kit.featureConfigNullable(flag, k)
                            addToLore("${CC.WHITE}- ${CC.GRAY}$k: ${CC.WHITE}${
                                if (configured != null) "$configured ${CC.GRAY}($v)" else v
                            }")
                        }

                        addToLore("")
                    }

                    addToLore("${CC.GREEN}Click to toggle!")
                }
                .toButton { _, _ ->
                    if (lifecycle != GameLifecycle.MiniGame)
                    {
                        if (lifecycle !in flag.availableLifecycles)
                        {
                            Button.playFail(player)
                            return@toButton
                        }
                    }

                    if (isIncompatible.isNotEmpty())
                    {
                        Button.playFail(player)
                        return@toButton
                    }

                    if (notApplicableYet.isNotEmpty())
                    {
                        Button.playFail(player)
                        return@toButton
                    }

                    if (kit.features(flag))
                    {
                        Button.playSuccess(player)
                        kit.features.remove(flag)

                        with(KitService.cached()) {
                            KitService.cached().kits[kit.id] = kit
                            KitService.sync(this)

                            openMenu(player)
                        }
                        return@toButton
                    }

                    Button.playSuccess(player)
                    kit.features[flag] = mutableMapOf()

                    with(KitService.cached()) {
                        KitService.cached().kits[kit.id] = kit
                        KitService.sync(this)

                        openMenu(player)
                    }
                }
        }

        return buttons
    }

    override fun getPrePaginatedTitle(player: Player) = "Editing features of ${kit.id}..."
    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose)
        {
            Tasks.delayed(1L) {
                SpecificKitEditorMenu(kit).openMenu(player)
            }
        }
    }
}
