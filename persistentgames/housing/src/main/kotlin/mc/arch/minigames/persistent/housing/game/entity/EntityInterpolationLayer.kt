package mc.arch.minigames.persistent.housing.game.entity

import mc.arch.minigames.persistent.housing.api.entity.HousingHologram
import mc.arch.minigames.persistent.housing.api.entity.HousingNPC
import mc.arch.minigames.persistent.housing.game.spatial.toLocation
import mc.arch.minigames.persistent.housing.game.toBukkitStack
import net.evilblock.cubed.entity.hologram.HologramEntity
import net.evilblock.cubed.entity.item.FloatingItem
import net.evilblock.cubed.entity.npc.NpcEntity
import org.bukkit.World

fun HousingNPC.toCubedNPC(world: World): NpcEntity = NpcEntity(this.aboveHeadText, this.location.toLocation(world))
    .also { npc ->
        npc.updateTexture(this.skinTexture, this.skinSignature)

        if (this.command != null)
        {
            npc.command = this.command
        }

        npc.messages = this.messagesToSend
        npc.persistent = false
    }

fun HousingHologram.toCubedHologram(world: World): HologramEntity =
    HologramEntity(this.name, this.location.toLocation(world))
        .also { hologram ->
            hologram.updateLines(this.lines)

            if (this.floatingItem != null)
            {
                hologram.setFloatingItem(
                    FloatingItem(
                        this.floatingItem!!.toBukkitStack(),
                        this.location.toLocation(world)
                    )
                )
            }

            hologram.persistent = false
        }