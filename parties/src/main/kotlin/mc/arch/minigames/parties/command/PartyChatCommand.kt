package mc.arch.minigames.parties.command

import gg.scala.basics.plugin.profile.BasicsProfileService
import gg.scala.basics.plugin.settings.defaults.values.StateSettingValue
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.Optional
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.listener.PlayerListener
import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.lemon.util.QuickAccess
import mc.arch.minigames.parties.model.PartyRole
import mc.arch.minigames.parties.model.PartySetting
import mc.arch.minigames.parties.toParty
import net.evilblock.cubed.util.CC

/**
 * @author GrowlyX
 * @since 5/7/2022
 */
@AutoRegister
object PartyChatCommand : ScalaCommand()
{
    @CommandAlias("pc|partychat|pchat|togglepartychat|tpc")
    fun onPartyChat(player: ScalaPlayer, @Optional message: String?)
    {
        if (message != null)
        {
            val profile = PlayerHandler.find(player.uniqueId)
                ?: return

            if (profile.findApplicablePunishment(PunishmentCategory.MUTE) != null)
            {
                throw ConditionFailedException("You may not send party chat messages if you are muted!")
            }

            val party = player.bukkit().toParty()
                ?: throw ConditionFailedException(
                    "You are not in a party right now!"
                )

            if (party.isEnabled(PartySetting.CHAT_MUTED))
            {
                if (!party.findMember(player.uniqueId)!!.role.over(PartyRole.MODERATOR))
                {
                    throw ConditionFailedException("The party chat is muted right now!")
                }
            }

            // TODO: chat filter
            QuickAccess.sendChannelMessage(
                channelId = "party",
                message = message,
                sender = PlayerHandler.find(player.uniqueId)!!
            )
            return
        }

        val profile = BasicsProfileService.find(player.bukkit())
            ?: throw ConditionFailedException(
                "Sorry, your profile did not load properly."
            )

        val messagesRef = profile.settings["party_chat"]!!
        val mapped = messagesRef.map<StateSettingValue>()

        if (mapped == StateSettingValue.ENABLED)
        {
            messagesRef.value = "DISABLED"

            player.sendMessage(
                "${CC.RED}You will no longer automatically send chat messages to your party."
            )
        } else
        {
            messagesRef.value = "ENABLED"

            player.sendMessage(
                "${CC.GREEN}You will now automatically send chat messages to your party."
            )
        }

        profile.save()
    }
}
