package gg.tropic.practice.commands

import gg.scala.basics.plugin.profile.BasicsProfileService
import gg.scala.basics.plugin.settings.defaults.values.StateSettingValue
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.tropic.practice.minigame.MinigameLobby
import gg.tropic.practice.settings.DuelsSettingCategory
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.visibility.VisibilityHandler
import org.bukkit.entity.Player

/**
 * @author Elb1to
 * @since 10/18/2023
 */
@AutoRegister
object TogglePlayerVisibilityCommand : ScalaCommand()
{
    @CommandAlias(
        "tpv|toggleplayervisibility|togglevisibility"
    )
    fun onToggleVisibility(player: Player)
    {
        val profile = BasicsProfileService.find(player)
            ?: throw ConditionFailedException(
                "Sorry, your profile did not load properly."
            )

        val spawnVisibility = profile.settings["${DuelsSettingCategory.DUEL_SETTING_PREFIX}:spawn-visibility-def"]!!
        val mapped = spawnVisibility.map<StateSettingValue>()

        if (mapped == StateSettingValue.ENABLED)
        {
            spawnVisibility.value = "DISABLED"
            player.sendMessage(
                "${CC.RED}You can no longer see players ${if (MinigameLobby.isMinigameLobby() || MinigameLobby.isMainLobby()) "in the lobby" else "at spawn"}."
            )
        } else
        {
            spawnVisibility.value = "ENABLED"
            player.sendMessage(
                "${CC.GREEN}You can now see players ${if (MinigameLobby.isMinigameLobby() || MinigameLobby.isMainLobby()) "in the lobby" else "at spawn"}."
            )
        }

        VisibilityHandler.update(player)
        profile.save()
    }
}
