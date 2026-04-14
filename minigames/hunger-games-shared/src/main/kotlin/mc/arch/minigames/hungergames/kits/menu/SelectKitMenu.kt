package mc.arch.minigames.hungergames.kits.menu

import com.cryptomorin.xseries.XMaterial
import mc.arch.minigames.hungergames.kits.HungerGamesKitDataSync
import mc.arch.minigames.hungergames.profile.HungerGamesProfileService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.math.Numbers
import org.bukkit.entity.Player

/**
 * @author ArchMC
 */
class SelectKitMenu : Menu("Select a Kit...")
{
    override fun size(buttons: Map<Int, Button>) = 45

    init
    {
        shouldLoadInSync()
    }

    override fun getButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf(
            4 to ItemBuilder
                .of(XMaterial.BOOK)
                .name("${CC.GREEN}Kit Selection")
                .addToLore(
                    "${CC.GRAY}You are viewing Survival",
                    "${CC.GRAY}Games kits!",
                    "",
                    "${CC.GRAY}Each kit has multiple levels.",
                    "${CC.GRAY}Higher levels give better gear.",
                    "${CC.GRAY}Purchase levels to unlock them!",
                    "",
                    "${CC.I_WHITE}Left-Click a kit to select it!",
                    "${CC.I_WHITE}Right-Click a kit to view contents!"
                )
                .toButton()
        )

        val profile = HungerGamesProfileService.find(player)
        val slots = (10..16) + (19..25) + (28..34)
        val kits = HungerGamesKitDataSync.cached().kits.values.toList()

        slots.withIndex().forEach { slot ->
            val kit = kits.getOrNull(slot.index)
                ?: return@forEach

            val isSelected = profile?.selectedKit == kit.id
            val highestOwned = profile?.highestOwnedLevel(kit.id, kit.maxLevel()) ?: 1
            val totalLevels = kit.levels.size

            // Find the next level the player can buy
            val nextUnownedLevel = kit.levels.entries
                .sortedBy { it.key }
                .firstOrNull { profile?.hasKit(kit.id, it.key) != true }

            buttons[slot.value] = runCatching {
                ItemBuilder.copyOf(kit.icon)
            }.getOrElse {
                ItemBuilder.of(XMaterial.BARRIER)
            }
                .name("${CC.GREEN}${kit.displayName}")
                .addToLore(
                    "${CC.GRAY}Levels Owned: ${CC.WHITE}$highestOwned${CC.GRAY}/$totalLevels",
                )
                .apply {
                    if (isSelected)
                    {
                        addToLore(
                            "",
                            "${CC.GREEN}✔ Currently Selected",
                            "${CC.GRAY}Level: ${CC.WHITE}${profile?.selectedKitLevel ?: 1}"
                        )
                    }

                    if (nextUnownedLevel != null)
                    {
                        addToLore(
                            "",
                            "${CC.GOLD}Next Level: ${CC.WHITE}Lv.${nextUnownedLevel.key}",
                            "${CC.GOLD}Cost: ${CC.YELLOW}${Numbers.format(nextUnownedLevel.value.price)} Coins"
                        )
                    } else
                    {
                        addToLore(
                            "",
                            "${CC.GREEN}✔ All levels unlocked!"
                        )
                    }
                }
                .addToLore(
                    "",
                    "${CC.YELLOW}Left-Click to select!",
                    "${CC.AQUA}Right-Click to view contents!"
                )
                .toButton { _, click ->
                    if (click?.isRightClick == true)
                    {
                        ViewKitContentsMenu(kit).openMenu(player)
                        return@toButton
                    }

                    // Left-click: select kit at the highest owned level
                    val prof = HungerGamesProfileService.find(player)
                        ?: return@toButton

                    Button.playNeutral(player)

                    val selectLevel = prof.highestOwnedLevel(kit.id, kit.maxLevel())
                    prof.selectedKit = kit.id
                    prof.selectedKitLevel = selectLevel
                    prof.save()

                    player.sendMessage(
                        "${CC.GREEN}You selected the ${CC.GOLD}${kit.displayName}${CC.GREEN} kit at level ${CC.GOLD}$selectLevel${CC.GREEN}!"
                    )
                    player.closeInventory()
                }
        }

        return buttons
    }
}
