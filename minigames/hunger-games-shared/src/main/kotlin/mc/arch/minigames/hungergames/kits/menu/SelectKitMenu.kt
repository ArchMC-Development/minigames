package mc.arch.minigames.hungergames.kits.menu

import com.cryptomorin.xseries.XMaterial
import mc.arch.minigames.hungergames.kits.HungerGamesKitDataSync
import mc.arch.minigames.hungergames.profile.HungerGamesProfileService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
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
                    "",
                    "${CC.I_WHITE}Select a kit by clicking",
                    "${CC.I_WHITE}the items below!"
                )
                .toButton()
        )

        val slots = (10..16) + (19..25) + (28..34)
        val kits = HungerGamesKitDataSync.cached().kits.values.toList()

        slots.withIndex().forEach { slot ->
            val kit = kits.getOrNull(slot.index)
                ?: return@forEach

            buttons[slot.value] = runCatching {
                ItemBuilder.copyOf(kit.icon)
            }.getOrElse {
                ItemBuilder.of(XMaterial.BARRIER)
            }
                .name("${CC.GREEN}${kit.displayName}")
                .addToLore(
                    "${CC.GRAY}Levels: ${kit.levels.size}",
                    "",
                    "${CC.YELLOW}Click to select!"
                )
                .toButton { _, _ ->
                    val profile = HungerGamesProfileService.find(player)
                        ?: return@toButton

                    Button.playNeutral(player)

                    profile.selectedKit = kit.id
                    profile.selectedKitLevel = 1
                    profile.save()

                    player.sendMessage("${CC.GREEN}You selected the ${CC.GOLD}${kit.displayName}${CC.GREEN} kit!")
                    player.closeInventory()
                }
        }

        return buttons
    }
}
