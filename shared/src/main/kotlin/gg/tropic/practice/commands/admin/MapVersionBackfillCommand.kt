package gg.tropic.practice.commands.admin

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.tropic.practice.map.MapService
import gg.tropic.practice.provider.MiniProviderVersion
import net.evilblock.cubed.util.CC

/**
 * Backfills a `version` field on every Map document missing it (legacy default).
 * Run once per environment after deploying the version-aware Map model.
 */
@AutoRegister
object MapVersionBackfillCommand : ScalaCommand()
{
    @CommandAlias("mapversionbackfill")
    @CommandPermission("op")
    fun onBackfill(player: ScalaPlayer)
    {
        val cached = MapService.cached()
        var touched = 0

        cached.maps.values.forEach { map ->
            // Property is non-null on the data class; the JSON deserializer applied the
            // LEGACY default when the field was missing in MongoDB. Only re-sync if
            // we want to materialize that default explicitly. We always do, so the
            // serialized form has the field and downstream JSON readers see it.
            if (map.version == MiniProviderVersion.LEGACY)
            {
                touched++
            }
        }

        MapService.sync(cached)

        player.sendMessage(
            "${CC.GREEN}Backfill complete. ${CC.WHITE}$touched${CC.GREEN} maps confirmed at " +
                "${CC.WHITE}${MiniProviderVersion.LEGACY}${CC.GREEN}; full container re-synced."
        )
    }
}
