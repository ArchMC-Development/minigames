package gg.tropic.practice.commands.admin

import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Optional
import gg.scala.commons.acf.bukkit.contexts.OnlinePlayer
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import me.lucko.helper.utils.Players
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.Reflection
import net.evilblock.cubed.util.ServerVersion
import net.minecraft.server.v1_8_R3.EntityTrackerEntry
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer

/**
 * @author Subham
 * @since 8/15/25
 */
@AutoRegister
object EntityTrackerDebugCommand : ScalaCommand()
{
    var shouldReconcile = true

    @CommandAlias("entitytrackerreconcile")
    @CommandPermission("op")
    fun onEntityTrackerReconcile(player: ScalaPlayer)
    {
        shouldReconcile = !shouldReconcile
        player.sendMessage(shouldReconcile.toString())
    }

    @CommandAlias("entitytrackerdebug")
    @CommandPermission("op")
    fun onEntityTrackerDebug(player: ScalaPlayer, @Optional other: OnlinePlayer?)
    {
        if (!ServerVersion.getVersion().isOlderThan(ServerVersion.v1_10_R1))
        {
            throw ConditionFailedException("This command only works on Legacy lobbies!")
        }

        if (other != null)
        {
            //player.sendMessage("${CC.WHITE}${other.player.name}${CC.PINK} is viewing ${other.player.custom().tracker.viewedPlayers.size}")
//            other.player.custom().tracker.viewedPlayers.forEach {
//                player.sendMessage("${CC.GRAY}- ${CC.WHITE}${it.name} ${CC.GRAY}(ONL: ${it.isOnline}) (ID: ${it.uniqueId}) (PID: ${(it as CraftPlayer).handle.profile.id})")
//            }
            return
        }

        Players.all().forEach { other ->
            //player.sendMessage("${CC.WHITE}${other.name}${CC.PINK} is viewing ${other.player.custom().tracker.viewedPlayers.size}")
        }
    }
}
