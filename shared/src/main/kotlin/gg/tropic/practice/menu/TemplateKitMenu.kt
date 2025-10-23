package gg.tropic.practice.menu

import gg.tropic.practice.kit.Kit
import gg.tropic.practice.kit.KitService
import gg.tropic.practice.kit.feature.FeatureFlag
import gg.tropic.practice.kit.feature.GameLifecycle
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.text.TextSplitter
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemFlag
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * @author GrowlyX
 * @since 9/23/2023
 */
abstract class TemplateKitMenu(private val viewer: Player, private val dynamic: Boolean = false) : PaginatedMenu()
{
    init
    {
        placeholder = true
    }

    open fun shouldIncludeKitDescription(): Boolean = true

    abstract fun filterDisplayOfKit(player: Player, kit: Kit): Boolean
    abstract fun itemDescriptionOf(player: Player, kit: Kit): List<String>
    abstract fun itemClicked(player: Player, kit: Kit, type: ClickType)

    open fun itemTitleFor(player: Player, kit: Kit): String
    {
        return "${CC.GREEN}${kit.displayName}${
            if (kit.features(FeatureFlag.Development))
                " ${CC.B_RED}(DEV)"
            else
                if (kit.features(FeatureFlag.NewlyCreated))
                    " ${CC.B_GOLD}NEW!" else ""
        }"
    }

    private val filteredKits by lazy {
        KitService
            .cached()
            .kits
            .values
            .filter {
                if (it.lifecycleType == GameLifecycle.MiniGame)
                {
                    // Don't show minigame kits, as they are internal
                    return@filter false
                }

                val developmentFilter =
                    viewer.hasPermission("practice.development-kits") || !it.features(FeatureFlag.Development)
                it.enabled && filterDisplayOfKit(viewer, it) && developmentFilter
            }
    }

    fun getRowsForKits() = if (dynamic) ceil(filteredKits.size.toDouble() / 7.0).toInt() else 3

    override fun size(buttons: Map<Int, Button>) = 18 + (getRowsForKits() * 9).coerceIn(9..27)
    override fun getMaxItemsPerPage(player: Player) = (getRowsForKits() * 7).coerceIn(7..21)

    private val availableItemRows = listOf(10..16, 19..25, 28..34)

    override fun getAllPagesButtonSlots() = availableItemRows
        .take(getRowsForKits().toInt().coerceIn(1..3))
        .flatMap { it.toList() }

    open fun getItemAmount(player: Player, kit: Kit): Int
    {
        return 1
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        filteredKits
            .sortedByDescending {
                it.featureConfig(FeatureFlag.MenuOrderWeight, "weight")
                    .toInt()
            }
            .forEach {
                buttons[buttons.size] = ItemBuilder
                    .copyOf(it.displayIcon)
                    .amount(getItemAmount(player, it))
                    .name(
                        itemTitleFor(player, it)
                    )
                    .apply {
                        if (it.description.isNotBlank() && shouldIncludeKitDescription())
                        {
                            setLore(
                                TextSplitter.split(
                                    it.description,
                                    CC.GRAY, " "
                                )
                            )
                            addToLore("")
                        }
                    }
                    .addFlags(
                        ItemFlag.HIDE_ATTRIBUTES,
                        ItemFlag.HIDE_ENCHANTS,
                        ItemFlag.HIDE_POTION_EFFECTS
                    )
                    .addToLore(
                        *itemDescriptionOf(player, it)
                            .toTypedArray()
                    )
                    .toButton { _, type ->
                        itemClicked(player, it, type!!)
                    }
            }

        return buttons
    }
}
