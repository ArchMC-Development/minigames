package gg.tropic.practice.minigame

import gg.scala.basics.plugin.profile.BasicsProfileService
import gg.scala.basics.plugin.settings.defaults.values.StateSettingValue
import gg.scala.commons.playerstatus.isVirtuallyInvisibleToSomeExtent
import gg.scala.staff.ScalaStaffPlugin
import gg.tropic.practice.configuration.PracticeConfigurationService
import gg.tropic.practice.kit.KitService
import gg.tropic.practice.minigame.menu.MinigameMapSelectorMenu
import gg.tropic.practice.player.LobbyPlayerService
import gg.tropic.practice.player.PlayerState
import gg.tropic.practice.queue.QueueService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 6/28/25
 */
fun MiniGameModeMetadata.joinGame(player: Player, configuration: MiniGameQueueConfiguration? = null)
{
    val lobbyPlayer = LobbyPlayerService
        .find(player.uniqueId)
        ?: return

    if (lobbyPlayer.state == PlayerState.InQueue)
    {
        player.sendMessage("${CC.RED}You are already queued for a game right now!")
        return
    }

    if (player.isVirtuallyInvisibleToSomeExtent())
    {
        player.sendMessage("${CC.RED}You are currently in vanish! Use ${CC.B}/vanish${CC.RED} to be able to join a queue.")
        return
    }

    val basicsProfile = BasicsProfileService.find(player)
    if (basicsProfile != null && player.hasPermission(ScalaStaffPlugin.STAFF_NODE))
    {
        if (basicsProfile.setting("auto-vanish", StateSettingValue.DISABLED) == StateSettingValue.ENABLED)
        {
            player.sendMessage("${CC.RED}You currently have AutoVanish enabled! Use ${CC.B}/toggleautovanish${CC.RED} to be able to join a queue.")
            return
        }
    }

    val queueId = toQueueId()
    val kit = KitService.cached().kits[queueId.kitID]
        ?: return run {
            player.sendMessage("${CC.RED}This mode is unavailable!")
        }

    // Check if player's party has Private Games mode enabled
    var finalConfiguration = configuration
    if (lobbyPlayer.isInParty())
    {
        val party = lobbyPlayer.partyOf()
        val privateGamesEnabled = party.delegate.isEnabled(mc.arch.minigames.parties.model.PartySetting.PRIVATE_GAMES)

        if (privateGamesEnabled)
        {
            // Create or modify configuration with private game settings
            finalConfiguration = configuration?.copy(
                isPrivateGame = true,
                privateGameSettings = gg.tropic.practice.privategames.PrivateGameSettings.default()
            ) ?: MiniGameQueueConfiguration(
                isPrivateGame = true,
                privateGameSettings = gg.tropic.practice.privategames.PrivateGameSettings.default()
            )

            player.sendMessage("${CC.GRAY}Starting a Private Game for your party...")
        }
    }

    QueueService.joinQueue(kit, queueId.queueType, queueId.teamSize, player, finalConfiguration)

    val messagePrefix = if (finalConfiguration?.isPrivateGame == true)
        "${CC.LIGHT_PURPLE}Creating a private"
    else
        "${CC.GREEN}Joining a"
    player.sendMessage("$messagePrefix $displayName game...")
}

fun ItemBuilder.toConciseJoinButton(mode: String) = toButton { player, type ->
    Button.playNeutral(player!!)

    val mode = PracticeConfigurationService
        .minigameType().provide()
        .mode(mode)

    if (type!!.isRightClick && mode.allowMapSelection)
    {
        MinigameMapSelectorMenu(
            {
                MinigameLobby.customizer().playProvider(player)
            },
            mode
        ).openMenu(player)
        return@toButton
    }

    mode.joinGame(player)
    player.closeInventory()
}
