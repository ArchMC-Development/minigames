package mc.arch.minigames.parties.service.chat

import gg.scala.basics.plugin.profile.BasicsProfileService
import gg.scala.basics.plugin.settings.defaults.values.StateSettingValue
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.channel.ChatChannelBuilder
import gg.scala.lemon.channel.ChatChannelService
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.util.QuickAccess
import mc.arch.minigames.parties.model.PartyRole
import mc.arch.minigames.parties.model.PartySetting
import mc.arch.minigames.parties.service.NetworkPartyService
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.kyori.adventure.text.Component

/**
 * @author Subham
 * @since 7/2/25
 */
@Service
object NetworkPartyChatService
{
    @Configure
    fun configure()
    {
        val partyChannel = ChatChannelBuilder.newBuilder()
            .identifier("party")
            .format { sender, receiver, message, server, rank ->
                val lemonPlayer = PlayerHandler.find(sender)
                Component.text("${CC.BLUE}Party ${CC.D_GRAY}${Constants.DOUBLE_ARROW_RIGHT} ${CC.WHITE}${
                    lemonPlayer?.getColoredName(prefixIncluded = true)
                }${CC.WHITE}: $message")
            }
            .compose()

        partyChannel.override(140) {
            val profile = BasicsProfileService.find(it)
                ?: return@override false

            val messagesRef = profile.settings["party_chat"]!!
            val mapped = messagesRef.map<StateSettingValue>()

            val party = NetworkPartyService.findParty(it.uniqueId)
                ?: return@override false

            if (party.isEnabled(PartySetting.CHAT_MUTED))
            {
                if (!party.findMember(it.uniqueId)!!.role.over(PartyRole.MODERATOR))
                {
                    return@override false
                }
            }

            return@override mapped == StateSettingValue.ENABLED
        }

        partyChannel.monitor()
        partyChannel.distribute()
        partyChannel.displayToPlayer { sender, receiver ->
            val receiverParty = NetworkPartyService.findParty(receiver.uniqueId)
            val senderParty = NetworkPartyService.findParty(sender)

            receiverParty != null &&
                senderParty != null &&
                receiverParty.uniqueId == senderParty.uniqueId
        }

        ChatChannelService.register(partyChannel)
    }
}
