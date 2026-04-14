package mc.arch.minigames.hungergames.kits.menu

import com.cryptomorin.xseries.XMaterial
import mc.arch.minigames.hungergames.kits.HungerGamesKit
import mc.arch.minigames.hungergames.profile.HungerGamesProfileService
import me.lucko.helper.Schedulers
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * @author ArchMC
 */
class ViewKitContentsMenu(
    private val kit: HungerGamesKit
) : Menu("${kit.displayName} - Kit Contents")
{
    init
    {
        shouldLoadInSync()
    }

    override fun size(buttons: Map<Int, Button>) = 45

    override fun getButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()
        val profile = HungerGamesProfileService.find(player)
        val isSelected = profile?.selectedKit == kit.id

        // Header info item
        buttons[4] = ItemBuilder
            .of(XMaterial.BOOK)
            .name("${CC.GREEN}${kit.displayName}")
            .addToLore(
                "${CC.GRAY}Viewing kit contents for",
                "${CC.GRAY}each level of this kit.",
                "",
                "${CC.GRAY}Levels: ${CC.WHITE}${kit.levels.size}",
            )
            .apply {
                if (isSelected)
                {
                    addToLore(
                        "",
                        "${CC.GREEN}✔ Currently Selected"
                    )
                }
            }
            .addToLore(
                "",
                "${CC.I_WHITE}Click a level to select",
                "${CC.I_WHITE}the kit at that level!"
            )
            .toButton()

        // Level buttons - lay them out in the body of the menu
        val slots = (10..16) + (19..25) + (28..34)
        val sortedLevels = kit.levels.entries.sortedBy { it.key }

        sortedLevels.forEachIndexed { index, (level, kitLevel) ->
            if (index >= slots.size) return@forEachIndexed

            val isSelectedLevel = isSelected && profile?.selectedKitLevel == level

            buttons[slots[index]] = runCatching {
                ItemBuilder.copyOf(kit.icon)
            }.getOrElse {
                ItemBuilder.of(XMaterial.BARRIER)
            }
                .name("${CC.GOLD}Level $level")
                .amount(level.coerceIn(1, 64))
                .apply {
                    val loreLines = mutableListOf<String>()

                    // Show selected indicator
                    if (isSelectedLevel)
                    {
                        loreLines.add("${CC.GREEN}✔ Selected")
                        loreLines.add("")
                    }

                    // Show armor contents
                    val armorNames = listOf("Helmet", "Chestplate", "Leggings", "Boots")
                    val armorItems = kitLevel.armor
                        .mapIndexed { i, item -> armorNames[i] to item }
                        .filter { it.second != null && it.second!!.type != XMaterial.AIR.parseMaterial() }

                    if (armorItems.isNotEmpty())
                    {
                        loreLines.add("${CC.AQUA}Armor:")
                        armorItems.forEach { (slot, item) ->
                            loreLines.add("${CC.GRAY}  $slot: ${CC.WHITE}${formatItemName(item!!)}")
                        }
                        loreLines.add("")
                    }

                    // Show inventory contents
                    val inventoryItems = kitLevel.inventory
                        .filterNotNull()
                        .filter { it.type != XMaterial.AIR.parseMaterial() }

                    if (inventoryItems.isNotEmpty())
                    {
                        loreLines.add("${CC.YELLOW}Items:")

                        // Group duplicate items and show count
                        val grouped = inventoryItems.groupBy { formatItemName(it) }
                        grouped.forEach { (name, items) ->
                            val totalAmount = items.sumOf { it.amount }
                            if (totalAmount > 1)
                            {
                                loreLines.add("${CC.GRAY}  ${CC.WHITE}${name} ${CC.GRAY}x$totalAmount")
                            } else
                            {
                                loreLines.add("${CC.GRAY}  ${CC.WHITE}${name}")
                            }
                        }
                        loreLines.add("")
                    }

                    if (armorItems.isEmpty() && inventoryItems.isEmpty())
                    {
                        loreLines.add("${CC.RED}No items configured")
                        loreLines.add("")
                    }

                    // Click action lore
                    if (isSelectedLevel)
                    {
                        loreLines.add("${CC.GREEN}Currently selected!")
                    } else
                    {
                        loreLines.add("${CC.YELLOW}Click to select this level!")
                    }

                    setLore(loreLines)
                }
                .toButton { _, _ ->
                    val prof = HungerGamesProfileService.find(player)
                        ?: return@toButton

                    Button.playNeutral(player)

                    prof.selectedKit = kit.id
                    prof.selectedKitLevel = level
                    prof.save()

                    player.sendMessage(
                        "${CC.GREEN}You selected ${CC.GOLD}${kit.displayName}${CC.GREEN} at level ${CC.GOLD}$level${CC.GREEN}!"
                    )

                    // Refresh menu to update selection indicators
                    Schedulers
                        .sync()
                        .runLater({
                            ViewKitContentsMenu(kit).openMenu(player)
                        }, 1L)
                }
        }

        return buttons
    }

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose)
        {
            Schedulers
                .sync()
                .run {
                    SelectKitMenu().openMenu(player)
                }
        }
    }

    private fun formatItemName(item: ItemStack): String
    {
        // Use display name if it has custom meta, otherwise format the material name
        if (item.hasItemMeta() && item.itemMeta?.hasDisplayName() == true)
        {
            return "${CC.RESET}${item.itemMeta!!.displayName}"
        }

        return item.type.name
            .lowercase()
            .replace('_', ' ')
            .split(' ')
            .joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
    }
}
