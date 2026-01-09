package mc.arch.minigames.persistent.housing.game.settings.type

import gg.scala.basics.plugin.settings.SettingValue
import org.bukkit.entity.Player

enum class MusicSetting(
    override val displayName: String,
) : SettingValue
{
    SHOULD_PLAY("Should Play"),
    SHOULDNT_PLAY("Shouldn't Play");

    override fun display(player: Player) = true
}