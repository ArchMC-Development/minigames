package mc.arch.minigames.hungergames.kits.menu

import com.cryptomorin.xseries.XMaterial
import gg.tropic.game.extensions.economy.Accounts
import gg.tropic.game.extensions.economy.EconomyDataSync
import gg.tropic.game.extensions.economy.EconomyProfileService
import gg.tropic.game.extensions.economy.Transaction
import gg.tropic.game.extensions.economy.TransactionResult
import gg.tropic.game.extensions.economy.TransactionService
import gg.tropic.game.extensions.economy.TransactionType
import mc.arch.minigames.hungergames.kits.HungerGamesKit
import mc.arch.minigames.hungergames.profile.HungerGamesProfile
import mc.arch.minigames.hungergames.profile.HungerGamesProfileService
import me.lucko.helper.Schedulers
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.math.Numbers
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * @author ArchMC
 */
class ViewKitContentsMenu(
    private val kit: HungerGamesKit
) : Menu("${kit.displayName} - Kit Contents")
{
    companion object
    {
        private const val ECONOMY_ID = "hunger-games-coins"
    }

    init
    {
        placeholder = true
        shouldLoadInSync()
    }

    override fun size(buttons: Map<Int, Button>) = 45

    override fun getButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()
        val profile = HungerGamesProfileService.find(player)
        val killRequirement = HungerGamesProfile.killRequirement(kit.id)
        val meetsKillReq = profile?.meetsKillRequirement(kit.id) ?: (killRequirement <= 0L)

        // Get economy info for balance display
        val economy = EconomyDataSync.cached().economies[ECONOMY_ID]
        val economyProfile = EconomyProfileService.find(player)
        val balance = economyProfile?.balance(ECONOMY_ID) ?: 0L

        // Header info item
        buttons[4] = ItemBuilder
            .of(XMaterial.BOOK)
            .name("${CC.GREEN}${kit.displayName}")
            .addToLore(
                "${CC.GRAY}Viewing kit contents for",
                "${CC.GRAY}each level of this kit.",
                "",
                "${CC.GRAY}Levels: ${CC.WHITE}${kit.levels.size}",
                "",
                "${CC.GRAY}Your Balance: ${
                    economy?.format(balance) ?: "${CC.GOLD}${Numbers.format(balance)} Coins"
                }",
            )
            .apply {
                if (killRequirement > 0L)
                {
                    addToLore(
                        "",
                        if (meetsKillReq) "${CC.GREEN}✔ Kill requirement met!"
                        else "${CC.RED}✖ Requires ${CC.YELLOW}${Numbers.format(killRequirement)} kills ${CC.RED}to unlock",
                        "${CC.GRAY}Your Kills: ${CC.WHITE}${Numbers.format(profile?.totalKills ?: 0L)}"
                    )
                }
            }
            .addToLore(
                "",
                "${CC.I_WHITE}Click a level to purchase it!"
            )
            .toButton()

        // Level buttons
        val slots = (10..16) + (19..25) + (28..34)
        val sortedLevels = kit.levels.entries.sortedBy { it.key }

        sortedLevels.forEachIndexed { index, (level, kitLevel) ->
            if (index >= slots.size) return@forEachIndexed

            val isOwned = profile?.hasKit(kit.id, level) ?: (level <= 1)
            val price = kitLevel.price

            buttons[slots[index]] = runCatching {
                ItemBuilder.copyOf(kit.icon)
            }.getOrElse {
                ItemBuilder.of(XMaterial.BARRIER)
            }
                .name(
                    if (isOwned) "${CC.GREEN}Level $level"
                    else "${CC.RED}Level $level ${CC.GRAY}(Locked)"
                )
                .amount(level.coerceIn(1, 64))
                .apply {
                    val loreLines = mutableListOf<String>()

                    // Show ownership status
                    if (isOwned)
                    {
                        loreLines.add("${CC.GREEN}✔ Owned")
                        loreLines.add("")
                    } else
                    {
                        loreLines.add("${CC.GRAY}Price: ${CC.GOLD}${Numbers.format(price)} Coins")
                        if (balance >= price)
                        {
                            loreLines.add("${CC.GREEN}You can afford this!")
                        } else
                        {
                            loreLines.add("${CC.RED}You need ${CC.YELLOW}${Numbers.format(price - balance)} ${CC.RED}more coins!")
                        }
                        loreLines.add("")
                    }

                    // Show armor contents
                    val armorNames = listOf("Helmet", "Chestplate", "Leggings", "Boots")
                    val armorItems = kitLevel.armor
                        .reversed()
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
                    if (!isOwned)
                    {
                        loreLines.add("${CC.GOLD}Click to purchase for ${CC.YELLOW}${Numbers.format(price)} Coins${CC.GOLD}!")
                    }

                    setLore(loreLines)
                }
                .toButton { _, _ ->
                    val prof = HungerGamesProfileService.find(player)
                        ?: return@toButton

                    if (prof.hasKit(kit.id, level))
                    {
                        // Already owned — nothing to do here
                        return@toButton
                    }

                    // Check kill requirement
                    if (!prof.meetsKillRequirement(kit.id))
                    {
                        val required = HungerGamesProfile.killRequirement(kit.id)
                        Button.playFail(player)
                        player.sendMessage(
                            "${CC.RED}You need ${CC.GOLD}${Numbers.format(required)} kills${CC.RED} to unlock this kit! You have ${CC.GOLD}${Numbers.format(prof.totalKills)}${CC.RED}."
                        )
                        return@toButton
                    }

                    // Need to purchase
                    val currentBalance = EconomyProfileService.find(player)
                        ?.balance(ECONOMY_ID) ?: 0L

                    if (currentBalance < price)
                    {
                        Button.playFail(player)
                        player.sendMessage(
                            "${CC.RED}You don't have enough coins! You need ${CC.GOLD}${
                                Numbers.format(price - currentBalance)
                            }${CC.RED} more coins."
                        )
                        return@toButton
                    }

                    // Check previous levels are owned
                    val previousLevel = level - 1
                    if (previousLevel > 1 && !prof.hasKit(kit.id, previousLevel))
                    {
                        Button.playFail(player)
                        player.sendMessage(
                            "${CC.RED}You must purchase level ${CC.GOLD}$previousLevel${CC.RED} first!"
                        )
                        return@toButton
                    }

                    // Submit purchase transaction
                    TransactionService.submit(
                        Transaction(
                            sender = player.uniqueId,
                            receiver = Accounts.SERVER,
                            type = TransactionType.Purchase,
                            economy = ECONOMY_ID,
                            amount = price
                        )
                    ).thenAccept { result ->
                        if (result != TransactionResult.Success)
                        {
                            player.sendMessage("${CC.RED}Transaction failed! Please try again.")
                            return@thenAccept
                        }

                        // Unlock the kit level
                        prof.unlockKit(kit.id, level)
                        prof.save()

                        Button.playNeutral(player)
                        player.sendMessage(
                            "${CC.GREEN}You purchased ${CC.GOLD}${kit.displayName} Level $level${CC.GREEN} for ${CC.GOLD}${
                                Numbers.format(price)
                            } Coins${CC.GREEN}!"
                        )

                        // Refresh menu
                        Schedulers.sync().runLater({
                            ViewKitContentsMenu(kit).openMenu(player)
                        }, 1L)
                    }
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
                    HungerGamesSelectKitMenu().openMenu(player)
                }
        }
    }

    private fun formatItemName(item: ItemStack): String
    {
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
