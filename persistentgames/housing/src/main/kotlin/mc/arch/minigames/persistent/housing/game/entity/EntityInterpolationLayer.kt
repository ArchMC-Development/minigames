package mc.arch.minigames.persistent.housing.game.entity

import mc.arch.minigames.persistent.housing.api.entity.HousingNPC
import mc.arch.minigames.persistent.housing.game.spatial.toLocation
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
    }