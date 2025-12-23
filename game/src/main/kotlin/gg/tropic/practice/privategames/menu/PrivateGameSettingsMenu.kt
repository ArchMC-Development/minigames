package gg.tropic.practice.privategames.menu

import com.cryptomorin.xseries.XMaterial
import gg.tropic.practice.games.GameImpl
import gg.tropic.practice.games.GameState
import gg.tropic.practice.privategames.settings.PrivateGameSetting
import gg.tropic.practice.privategames.settings.PrivateGameSettingsRegistry
import gg.tropic.practice.privategames.settings.impl.BooleanSetting
import gg.tropic.practice.privategames.settings.impl.DoubleSetting
import gg.tropic.practice.privategames.settings.impl.IntSetting
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView

/**
 * Dynamic menu for editing private game settings in the waiting lobby.
 * Automatically renders settings based on the game type.
 *
 * @author GrowlyX
 * @since 12/22/24
 */
class PrivateGameSettingsMenu(
    private val game: GameImpl,
    private val gameType: String
) : PaginatedMenu()
{
    // Load settings for this game type
    private val settings: MutableList<PrivateGameSetting<*>> = mutableListOf()

    init
    {
        updateAfterClick = true

        // Load settings from registry if not already stored in game
        val existingSettings = game.expectationModel.privateGameSettings?.gameSpecificSettings
        val freshSettings = PrivateGameSettingsRegistry.getSettingsFor(gameType)

        freshSettings.forEach { setting ->
            // Restore any previously saved values
            existingSettings?.get(setting.id)?.let { savedValue ->
                @Suppress("UNCHECKED_CAST")
                when (setting) {
                    is BooleanSetting -> setting.value = savedValue as? Boolean ?: setting.defaultValue
                    is IntSetting -> setting.value = savedValue as? Int ?: setting.defaultValue
                    is DoubleSetting -> setting.value = savedValue as? Double ?: setting.defaultValue
                }
            }
            settings.add(setting)
        }
    }

    override fun getPrePaginatedTitle(player: Player) = "Private Game Settings"

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        settings.forEachIndexed { index, setting ->
            buttons[index] = SettingButton(setting)
        }

        return buttons
    }

    override fun getMaxItemsPerPage(player: Player) = 21

    override fun onClose(player: Player, manualClose: Boolean)
    {
        // Save settings back to game expectation
        val privateSettings = game.expectationModel.privateGameSettings ?: return
        settings.forEach { setting ->
            privateSettings.gameSpecificSettings[setting.id] = setting.value as Any
        }
    }

    private inner class SettingButton(
        private val setting: PrivateGameSetting<*>
    ) : Button()
    {
        override fun getName(player: Player): String
        {
            return "${CC.GREEN}${setting.displayName}"
        }

        override fun getDescription(player: Player): List<String>
        {
            val lines = mutableListOf<String>()

            setting.description.forEach {
                lines.add("${CC.GRAY}$it")
            }

            lines.add("")
            lines.add("${CC.GRAY}Current: ${CC.AQUA}${formatValue(setting.value)}")
            lines.add("")

            when (setting) {
                is BooleanSetting -> {
                    lines.add("${CC.YELLOW}Click to toggle!")
                }
                is IntSetting, is DoubleSetting -> {
                    lines.add("${CC.YELLOW}Left-Click to increase")
                    lines.add("${CC.YELLOW}Right-Click to decrease")
                    lines.add("${CC.YELLOW}Middle-Click to reset")
                }
                else -> {
                    lines.add("${CC.YELLOW}Click to cycle!")
                }
            }

            return lines
        }

        override fun getMaterial(player: Player): XMaterial
        {
            return when (setting) {
                is BooleanSetting -> if (setting.value) XMaterial.LIME_DYE else XMaterial.GRAY_DYE
                is IntSetting -> XMaterial.CLOCK
                is DoubleSetting -> XMaterial.GOLD_NUGGET
                else -> XMaterial.PAPER
            }
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView)
        {
            if (!(game.state == GameState.Waiting || game.state == GameState.Starting))
            {
                player.sendMessage("${CC.RED}You can only modify settings before the game starts!")
                return
            }

            when (setting) {
                is BooleanSetting -> {
                    setting.cycleValue()
                    player.sendMessage("${CC.GREEN}${setting.displayName} ${CC.GRAY}set to ${CC.AQUA}${formatValue(setting.value)}")
                }
                is IntSetting -> {
                    when (clickType) {
                        ClickType.LEFT -> {
                            val options = setting.availableValues()
                            val currentIndex = options.indexOf(setting.value)
                            if (currentIndex < options.size - 1) {
                                setting.value = options[currentIndex + 1]
                            }
                        }
                        ClickType.RIGHT -> {
                            val options = setting.availableValues()
                            val currentIndex = options.indexOf(setting.value)
                            if (currentIndex > 0) {
                                setting.value = options[currentIndex - 1]
                            }
                        }
                        ClickType.MIDDLE -> setting.reset()
                        else -> {}
                    }
                    player.sendMessage("${CC.GREEN}${setting.displayName} ${CC.GRAY}set to ${CC.AQUA}${formatValue(setting.value)}")
                }
                is DoubleSetting -> {
                    when (clickType) {
                        ClickType.LEFT -> {
                            val options = setting.availableValues()
                            val currentIndex = options.indexOf(setting.value)
                            if (currentIndex < options.size - 1) {
                                setting.value = options[currentIndex + 1]
                            }
                        }
                        ClickType.RIGHT -> {
                            val options = setting.availableValues()
                            val currentIndex = options.indexOf(setting.value)
                            if (currentIndex > 0) {
                                setting.value = options[currentIndex - 1]
                            }
                        }
                        ClickType.MIDDLE -> setting.reset()
                        else -> {}
                    }
                    player.sendMessage("${CC.GREEN}${setting.displayName} ${CC.GRAY}set to ${CC.AQUA}${formatValue(setting.value)}")
                }
                else -> {
                    setting.cycleValue()
                    player.sendMessage("${CC.GREEN}${setting.displayName} ${CC.GRAY}set to ${CC.AQUA}${formatValue(setting.value)}")
                }
            }

            // Save to game settings immediately
            val privateSettings = game.expectationModel.privateGameSettings ?: return
            privateSettings.gameSpecificSettings[setting.id] = setting.value as Any
        }

        private fun formatValue(value: Any?): String
        {
            return when (value) {
                is Boolean -> if (value) "Enabled" else "Disabled"
                is Double -> "%.2f".format(value)
                else -> value?.toString() ?: "N/A"
            }
        }
    }
}
