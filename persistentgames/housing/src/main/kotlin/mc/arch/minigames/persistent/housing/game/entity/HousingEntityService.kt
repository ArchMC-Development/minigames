package mc.arch.minigames.persistent.housing.game.entity

import mc.arch.minigames.persistent.housing.api.model.PlayerHouse
import net.evilblock.cubed.entity.hologram.HologramEntity
import net.evilblock.cubed.entity.npc.NpcEntity
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player

object HousingEntityService
{
    private val holograms: MutableMap<Location, HologramEntity> = mutableMapOf()
    private val npcs: MutableMap<Location, NpcEntity> = mutableMapOf()

    fun configure(house: PlayerHouse, bukkitWorld: World)
    {
        house.houseNPCMap.values.forEach { npc ->
            val entity = npc.toCubedNPC(bukkitWorld)

            npcs[entity.location] = entity
        }

        house.houseHologramMap.values.forEach { hologram ->
            val entity = hologram.toCubedHologram(bukkitWorld)

            holograms[entity.location] = entity
        }

        bukkitWorld.players.toSet()
            .forEach {
                spawnEntities(it)
            }
    }

    fun destroyAll()
    {
        destroyHologramEntities()
        destroyNPCEntities()
    }

    private fun destroyHologramEntities()
    {
        holograms.values.forEach {
            it.destroyForCurrentWatchers()
        }

        holograms.clear()
    }

    private fun destroyNPCEntities()
    {
        npcs.values.forEach {
            it.destroyForCurrentWatchers()
        }

        npcs.clear()
    }

    fun spawnEntities(player: Player)
    {
        Tasks.sync {
            npcs.values.forEach {
                it.initializeData()
                it.spawn(player)
            }

            holograms.values.forEach {
                it.initializeData()
                it.spawn(player)
            }
        }
    }
}