package gg.tropic.practice.commands.admin

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Optional
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.scala.lemon.util.QuickAccess
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import gg.tropic.practice.kit.Kit
import gg.tropic.practice.profile.PracticeProfile
import gg.tropic.practice.profile.PracticeProfileService
import net.evilblock.cubed.util.CC
import org.bukkit.command.CommandSender

/**
 * @author Topu
 * @date 11/30/2024
 */
@AutoRegister
object ResetKitLoadoutCommand : ScalaCommand()
{
    enum class LoadoutReset(
        val resetFor: (PracticeProfile?, Kit?) -> Unit
    )
    {
        AllKitLoadouts({ profile, _ ->
            profile!!.customLoadouts.clear()
            profile.save()
        }),
        KitLoadout({ profile, kit ->
            profile!!.getLoadoutsFromKit(kit!!).clear()
            profile.save()
        })
    }

    @CommandAlias("resetloadout")
    @CommandPermission("practice.command.resetloadout")
    fun onResetLoadout(
        player: CommandSender,
        resetOptions: LoadoutReset,
        @Optional target: ScalaPlayer?,
        @Optional kit: Kit?
    )
    {
        val profile: PracticeProfile? = PracticeProfileService.find(target!!.uniqueId)

        resetOptions.resetFor(profile, kit)
        player.sendMessage(
            "${CC.GREEN}You have reset the ${CC.BOLD}${resetOptions.name}${CC.GREEN} for ${
                if (profile != null) "${CC.WHITE}${
                    QuickAccess.nameOrConsole(
                        target.uniqueId
                    )
                }${CC.GREEN}" else ""
            }${if (kit != null) " on kit ${CC.PRI}${kit.id}${CC.GREEN}" else ""}."
        )
    }
}
