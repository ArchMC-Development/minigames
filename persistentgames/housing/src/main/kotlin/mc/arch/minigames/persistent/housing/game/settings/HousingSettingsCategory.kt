package mc.arch.minigames.persistent.housing.game.settings

import gg.scala.basics.plugin.settings.SettingCategory
import gg.scala.basics.plugin.settings.SettingContainer
import gg.scala.basics.plugin.settings.SettingMenu
import gg.scala.commons.annotations.plugin.SoftDependency
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan
import mc.arch.minigames.persistent.housing.game.settings.type.MusicSetting
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player


@Service
@IgnoreAutoScan
@SoftDependency("ScBasics")
object HousingSettingsCategory : SettingCategory
{
    val SETTING_PREFIX = "realms"

    override val items = listOf(
        SettingContainer.buildEntry {
            id = "$SETTING_PREFIX:music-settings"
            displayName = "Music Settings"

            clazz = MusicSetting::class.java
            default = MusicSetting.SHOULD_PLAY

            description += "Allows you to change if you can"
            description += "hear this realm's music."

            item = ItemBuilder.of(Material.JUKEBOX)
        },
    )

    @Configure
    fun configure()
    {
        SettingMenu.defaultCategory = "Realms"
    }

    override fun display(player: Player) = true
    override val description = listOf(
        "Configure everything realms related!"
    )
    override val displayName = "Realms"
}