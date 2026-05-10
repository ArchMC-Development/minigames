package gg.tropic.practice.commands.admin

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.tropic.practice.map.MapService
import gg.tropic.practice.provider.MiniProviderVersion
import gg.tropic.practice.versioned.Versioned
import net.evilblock.cubed.util.CC

@AutoRegister
object MapVersionBackfillCommand : ScalaCommand()
{
    @CommandAlias("mapversionbackfill")
    @CommandPermission("op")
    fun onBackfill(player: ScalaPlayer)
    {
        val provider = Versioned.toProvider().getSlimeProvider()
        val cached = MapService.cached()

        var retagged = 0
        var unresolved = 0

        cached.maps.values.forEach { map ->
            val formatByte = provider.versionOf(map.associatedSlimeTemplate)
            if (formatByte == null)
            {
                unresolved++
                return@forEach
            }

            val detected = if (formatByte <= 9)
                MiniProviderVersion.LEGACY
            else
                MiniProviderVersion.MODERN

            if (map.version != detected)
            {
                map.version = detected
                retagged++
            }
        }

        MapService.sync(cached)

        player.sendMessage(
            "${CC.GREEN}Backfill complete. ${CC.WHITE}$retagged${CC.GREEN} maps retagged, " +
                "${CC.WHITE}$unresolved${CC.GREEN} unresolved (template not visible to this provider)."
        )
    }
}
