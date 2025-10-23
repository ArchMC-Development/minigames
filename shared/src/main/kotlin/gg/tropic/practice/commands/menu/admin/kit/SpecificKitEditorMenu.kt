package gg.tropic.practice.commands.menu.admin.kit

import com.cryptomorin.xseries.XMaterial
import gg.tropic.practice.commands.menu.admin.kit.display.EditKitDisplaysMenu
import gg.tropic.practice.commands.menu.admin.kit.groups.KitGroupEditorMenu
import gg.tropic.practice.commands.menu.admin.prevention.AllowRemoveItemsWithinInventory
import gg.tropic.practice.kit.Kit
import gg.tropic.practice.kit.KitService
import gg.tropic.practice.kit.administration.specific.KitFeaturesEditMenu
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffectType

class SpecificKitEditorMenu(val kit: Kit) : Menu(), AllowRemoveItemsWithinInventory
{
    init
    {
        placeholder = true
    }

    private val contentSlotList = listOf(
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34
    )

    override fun size(buttons: Map<Int, Button>): Int
    {
        return 54
    }

    override fun getTitle(player: Player): String
    {
        return "Edit Kit Properties"
    }

    override fun onOpen(player: Player)
    {
        player.inventory.clear()
        player.inventory.contents = kit.contents
        player.updateInventory()
    }

    override fun onClose(player: Player, manualClose: Boolean)
    {
        Tasks.sync {
            resetInventory(player)
        }
    }

    override fun getButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf<Int, Button>().also { map ->
            map[2] = ItemBuilder
                .of(XMaterial.EXPERIENCE_BOTTLE)
                .name("${CC.B_YELLOW}Features")
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

            map[3] = ItemBuilder.of(XMaterial.RED_DYE)
                .name("${CC.B_RED}Delete Kit")
                .addToLore(
                    "${CC.GRAY}Deletes this kit from",
                    "${CC.GRAY}the active selection.",
                    "",
                    "${CC.GREEN}Click to delete!"
                ).toButton { _, _ ->
                    with(KitService.cached()) {
                        KitService.cached().kits.remove(kit.id)
                        KitService.sync(this)
                    }

                    KitEditorSelectionMenu(player).openMenu(player)
                    player.sendMessage("${CC.RED}You have just deleted this kit..")
                }

            map[4] = ItemBuilder.of(XMaterial.LEAD)
                .name("${CC.B_AQUA}Kit Groups")
                .addToLore(
                    "${CC.GRAY}Edit the groups that you want",
                    "${CC.GRAY}to be correlated with this kit.",
                    "",
                    "${CC.YELLOW}Click to edit!"
                ).toButton { _, _ ->
                    KitGroupEditorMenu(kit).openMenu(player)
                }

            map[5] = ItemBuilder.of(XMaterial.LIME_DYE)
                .name("${CC.B_GREEN}Save Contents")
                .addToLore(
                    "${CC.GRAY}Sets the contents from your current",
                    "${CC.GRAY}inventory to the contents that will.",
                    "${CC.GRAY}be used for the kit.",
                    "",
                    "${CC.GREEN}Click to save!"
                ).toButton { _, _ ->
                    kit.contents = player.inventory.contents

                    with(KitService.cached()) {
                        KitService.cached().kits[kit.id] = kit
                        KitService.sync(this)
                    }

                    player.sendMessage("${CC.GREEN}Saved the default contents of this kit...")
                }

            map[6] = ItemBuilder.of(XMaterial.NAME_TAG)
                .name("${CC.BD_PURPLE}Change Display Elements")
                .addToLore(
                    "${CC.GRAY}Change some of the display elements",
                    "${CC.GRAY}of this kit such as the display name,",
                    "${CC.GRAY}icon, and description.",
                    "",
                    "${CC.GREEN}Click to edit!"
                ).toButton { _, _ ->
                    EditKitDisplaysMenu(kit).openMenu(player)
                }

            kit.contents
                .filterNotNull()
                .groupBy { item -> item }
                .entries.forEachIndexed { index, value ->
                    map[contentSlotList[index]] = ItemBuilder
                        .copyOf(value.key)
                        .toButton()
                }

            for (int in contentSlotList)
            {
                if (!map.containsKey(int))
                {
                    map[int] = ItemBuilder.of(Material.AIR).toButton()
                }
            }

            for ((armorIndex, int) in (47 until 51).withIndex())
            {
                val armorContent = kit.armorContents.getOrNull(armorIndex)

                map[int] = if (armorContent == null) ItemBuilder.of(XMaterial.BARRIER)
                    .name("${CC.RED}No Item")
                    .toButton()
                else ItemBuilder.copyOf(armorContent)
                    .toButton()
            }

            map[52] = ItemBuilder.of(XMaterial.BREWING_STAND)
                .name("${CC.B_GOLD}Potion Effects")
                .setLore(
                    if (kit.potionEffects.isEmpty())
                    {
                        listOf("${CC.RED}None!", "", "${CC.YELLOW}Click to add!")
                    } else
                    {
                        kit.potionEffects.map { "${CC.WHITE}${it.key} with an amplifier of ${it.value}" }.toMutableList().also {
                            it.add("")
                            it.add("${CC.YELLOW}Click to add more!")
                        }
                    }
                ).toButton { _, _ ->
                    InputPrompt()
                        .withText("${CC.GREEN}Enter the potion effect type ${CC.GRAY}(Case Insensitive) ${CC.GREEN}followed by the amplifier ${CC.GRAY}(Format: speed,1)")
                        .acceptInput { _, input ->
                            val parts = input.split(",")

                            if (parts.size != 2)
                            {
                                player.sendMessage("${CC.RED}Invalid usage!")
                                return@acceptInput
                            }

                            val potionEffect = PotionEffectType.getByName(parts[0].uppercase())

                            if (potionEffect == null)
                            {
                                player.sendMessage("${CC.RED}Invalid potion effect name!")
                                return@acceptInput
                            }

                            val amplifier = parts[1].toIntOrNull()

                            if (amplifier == null)
                            {
                                player.sendMessage("${CC.RED}Invalid amplifier number!")
                                return@acceptInput
                            }

                            kit.potionEffects[potionEffect.name] = amplifier

                            with(KitService.cached()) {
                                KitService.cached().kits[kit.id] = kit
                                KitService.sync(this)
                            }

                            SpecificKitEditorMenu(kit).openMenu(player)
                            player.sendMessage("${CC.GREEN}Added a potion effect to this kit!")
                        }.start(player)
                }
        }
    }

    private fun resetInventory(player: Player)
    {
        player.inventory.clear()
        player.updateInventory()
        player.performCommand("get-lobby-items")
    }
}
