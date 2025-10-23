package gg.tropic.practice.commands

import gg.scala.basics.plugin.profile.BasicsProfileService
import gg.scala.basics.plugin.settings.defaults.values.StateSettingValue
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.tropic.practice.settings.DuelsSettingCategory
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

/**
 * @author Elb1to
 * @since 10/19/2023
 */
object ToggleAutoAcceptQuestsCommand : ScalaCommand()
{
    @CommandAlias("toggleautoacceptquests|taaq")
    @CommandPermission("minigame.quests.autoactivate")
    fun onAutoAcceptToggle(player: Player): CompletableFuture<Void>
    {
        val profile = BasicsProfileService.find(player)
            ?: throw ConditionFailedException(
                "Sorry, your profile did not load properly."
            )

        val allowSpectators = profile.settings["${DuelsSettingCategory.DUEL_SETTING_PREFIX}:auto-accept-quests"]!!
        val mapped = allowSpectators.map<StateSettingValue>()

        if (mapped == StateSettingValue.ENABLED)
        {
            allowSpectators.value = "DISABLED"
            player.sendMessage(
                "${CC.RED}You will no longer automatically accept quests."
            )
        } else
        {
            allowSpectators.value = "ENABLED"
            player.sendMessage(
                "${CC.GREEN}You will no longer automatically accept quests."
            )
        }

        return profile.save()
    }
}
