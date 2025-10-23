package mc.arch.lobby.main.command

import com.cryptomorin.xseries.XMaterial
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.Conditions
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.tropic.practice.replacements.toTemplatePlayerCounts
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bungee.BungeeUtil
import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 7/29/25
 */
@AutoRegister
object LegacyJoinCommand : ScalaCommand()
{
    @CommandAlias("legacyjoin")
    fun onLegacyJoin(
        @Conditions("cooldown:duration=5,unit=SECONDS")
        player: ScalaPlayer,
        serverId: String
    )
    {
        if (!serverId.startsWith("legacy-"))
        {
            throw ConditionFailedException("You cannot use this command for this server instance!")
        }

        if (serverId == "legacy-survival")
        {
            object : Menu("Joining Survival...")
            {
                override fun getButtons(player: Player) = mapOf(
                    12 to ItemBuilder
                        .of(XMaterial.GRASS_BLOCK)
                        .name("${CC.GREEN}Old Survival")
                        .addToLore(
                            "${CC.GRAY}The Survival that you",
                            "${CC.GRAY}know and love!",
                            "",
                            "${CC.GRAY}Online: ${CC.WHITE}<legacyplayercount_legacy-survival>".toTemplatePlayerCounts(),
                            "",
                            "${CC.YELLOW}Click to play!"
                        )
                        .toButton { _, _ ->
                            Button.playNeutral(player)
                            player.closeInventory()
                            player.sendMessage("${CC.AQUA}Joining ${CC.BOLD}${
                                serverId
                                    .removePrefix("legacy-")
                                    .capitalize()
                            }${CC.AQUA}...")
                            BungeeUtil.sendToServer(player, serverId)
                        },
                    14 to ItemBuilder
                        .of(XMaterial.GRASS_BLOCK)
                        .glow()
                        .name("${CC.GREEN}New Survival")
                        .addToLore(
                            "${CC.GRAY}ArchMC's latest survival",
                            "${CC.GRAY}gamemode! Come try it out!",
                            "",
                            "${CC.GRAY}Online: ${CC.WHITE}<group_survival>".toTemplatePlayerCounts(),
                            "",
                            "${CC.YELLOW}Click to play!"
                        )
                        .toButton { _, _ ->
                            Button.playNeutral(player)
                            player.closeInventory()
                            player.performCommand("joinqueue survival")
                        }
                )
                override fun size(buttons: Map<Int, Button>) = 27
            }.openMenu(player)
            return
        }

        player.sendMessage("${CC.AQUA}Joining ${CC.BOLD}${
            serverId
                .removePrefix("legacy-")
                .capitalize()
        }${CC.AQUA}...")

        BungeeUtil.sendToServer(player.bukkit(), serverId)
    }
}
