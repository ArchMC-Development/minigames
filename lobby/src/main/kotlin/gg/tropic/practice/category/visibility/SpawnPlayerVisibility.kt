package gg.tropic.practice.category.visibility

import gg.scala.basics.plugin.profile.BasicsProfileService
import gg.scala.basics.plugin.settings.defaults.values.StateSettingValue
import gg.tropic.practice.settings.DuelsSettingCategory
import net.evilblock.cubed.visibility.VisibilityAction
import net.evilblock.cubed.visibility.VisibilityAdapter
import net.evilblock.cubed.visibility.VisibilityAdapterRegister
import org.bukkit.entity.Player

import gg.tropic.practice.games.GameService

/**
 * @author Elb1to
 * @since 10/18/2023
 */
@VisibilityAdapterRegister("duels-visibility-adapter")
object SpawnPlayerVisibility : VisibilityAdapter
{
    override fun getAction(
        toRefresh: Player, refreshFor: Player
    ): VisibilityAction
    {
        val profile = BasicsProfileService.find(refreshFor)
            ?: return VisibilityAction.NEUTRAL

        if (GameService.isSpectating(refreshFor)) {
            return VisibilityAction.NEUTRAL
        }

        val messagesRef = profile.settings["${DuelsSettingCategory.DUEL_SETTING_PREFIX}:spawn-visibility-def"]!!
        val visible = messagesRef.map<StateSettingValue>()

        return if (visible == StateSettingValue.DISABLED)
            VisibilityAction.HIDE else VisibilityAction.NEUTRAL
    }

    fun get(player: Player): Boolean
    {
        val profile = BasicsProfileService.find(player)
            ?: return false

        val messagesRef = profile.settings["${DuelsSettingCategory.DUEL_SETTING_PREFIX}:spawn-visibility-def"]!!
        val visible = messagesRef.map<StateSettingValue>()

        return visible == StateSettingValue.ENABLED
    }
}
