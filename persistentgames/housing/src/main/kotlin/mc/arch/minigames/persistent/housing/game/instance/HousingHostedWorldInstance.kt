package mc.arch.minigames.persistent.housing.game.instance

import gg.scala.lemon.handler.PlayerHandler
import gg.tropic.practice.extensions.unmount
import gg.tropic.practice.map.metadata.impl.MapSpawnMetadata
import gg.tropic.practice.map.metadata.impl.MapZoneMetadata
import gg.tropic.practice.map.utilities.MapMetadataScanUtilities
import gg.tropic.practice.schematics.SchematicUtil
import gg.tropic.practice.ugc.WorldInstanceProviderType
import gg.tropic.practice.ugc.generation.visits.VisitWorldRequest
import gg.tropic.practice.ugc.instance.BaseHostedWorldInstance
import mc.arch.minigames.persistent.housing.api.VisitHouseConfiguration
import mc.arch.minigames.persistent.housing.api.service.PlayerHousingService
import mc.arch.minigames.persistent.housing.game.entity.toCubedHologram
import mc.arch.minigames.persistent.housing.game.entity.toCubedNPC
import mc.arch.minigames.persistent.housing.game.getReference
import mc.arch.minigames.persistent.housing.game.item.HousingItemService
import mc.arch.minigames.persistent.housing.game.resources.HousingPlayerResources
import mc.arch.minigames.persistent.housing.game.schematic.HousingSchematicService
import mc.arch.minigames.persistent.housing.game.spatial.SpatialZoneService
import mc.arch.minigames.persistent.housing.game.spatial.toLocation
import mc.arch.minigames.persistent.housing.game.spatial.toWorldPosition
import mc.arch.minigames.persistent.housing.game.translateCC
import mc.arch.minigames.versioned.generics.worlds.LoadedSlimeWorld
import me.lucko.helper.Schedulers
import net.evilblock.cubed.entity.hologram.HologramEntity
import net.evilblock.cubed.entity.npc.NpcEntity
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

class HousingHostedWorldInstance(
    request: VisitWorldRequest,
    world: LoadedSlimeWorld
) : BaseHostedWorldInstance<HousingPlayerResources>(
    ownerPlayerId = request.ownerPlayerId,
    loadedWorld = world,
    providerType = WorldInstanceProviderType.REALM,
    persistence = null,
    globalId = request.worldGlobalId
)
{
    var configuration = (request.configuration as VisitHouseConfiguration)

    private val holograms: MutableMap<Location, HologramEntity> = mutableMapOf()
    private val npcs: MutableMap<Location, NpcEntity> = mutableMapOf()

    private val playerHouseReference get() = PlayerHousingService.cached(globalId)

    override fun onLoad()
    {
        val house = getHouse().join()

        if (house == null)
        {
            unload()
            return
        } else
        {
            PlayerHousingService.cache(house)
        }

        if (!house.hasBeenSetup)
        {
            val houseSchematic = HousingSchematicService.findSchematicsOf(house.map)
            val origin = Location(bukkitWorld, 0.0, 100.0, 0.0)

            SchematicUtil.pasteSchematic(origin, houseSchematic)
            Bukkit.getLogger().info("Pasting the selected map schematic for the first time: ${house.identifier}")

            house.hasBeenSetup = true
            house.save().join()
        }

        val metadata = MapMetadataScanUtilities.buildMetadataFor(bukkitWorld)
        Schedulers
            .sync()
            .run {
                metadata.metadataSignLocations.forEach {
                    bukkitWorld
                        .getBlockAt(it.toLocation(bukkitWorld))
                        .setType(
                            Material.AIR, true
                        )
                }

                bukkitWorld.players.forEach {
                    it.unmount()
                }
            }
            .join()

        metadata.metadata
            .filterIsInstance<MapSpawnMetadata>()
            .firstOrNull { it.id == "a" }
            ?.let { spawn ->
                if (house.spawnPoint == null)
                {
                    house.spawnPoint = spawn.position.toLocation(bukkitWorld).toWorldPosition()
                    house.save().join()
                }
            }

        val region = metadata.metadata
            .filterIsInstance<MapZoneMetadata>()
            .firstOrNull()

        if (region != null)
        {
            SpatialZoneService.configure(region, bukkitWorld)
        }

        reconfigureWorld(firstSetup = true).join()

        Schedulers
            .async()
            .runRepeating({ _ ->
                bukkitWorld.players.forEach { player ->
                    player.setPlayerTime(1000L, true)
                }
            }, 0L, 20L)
            .bindWith(this)
    }

    override fun onUnload()
    {
        destroyNPCEntities()
        destroyHologramEntities()
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

    private fun spawnEntities(player: Player)
    {
        Tasks.sync {
            npcs.values.forEach {
                println("Spawning NPC #${it.id} for ${player.name}")
                it.initializeData()
                it.spawn(player)
            }

            holograms.values.forEach {
                println("Spawning Hologram #${it.id} for ${player.name}")
                it.initializeData()
                it.spawn(player)
            }
        }
    }

    override fun generateScoreboardTitle(player: Player) = "${CC.BD_RED}REALMS"
    override fun generateScoreboardLines(player: Player) = listOf(
        "${CC.D_RED}Realm Name:",
        "${CC.WHITE}${playerHouseReference?.displayName ?: "${CC.RED}Unavailable"}",
        "",
        "${CC.D_RED}Guests:",
        "${CC.WHITE}${playerHouseReference?.getReference()?.onlinePlayers?.size ?: 0} out of ${playerHouseReference?.maxPlayers ?: 100}",
        "",
        "${CC.D_RED}Your Role:",
        "${CC.WHITE}${
            playerHouseReference?.getRole(player.uniqueId)?.coloredName()?.translateCC() ?: "${CC.GRAY}Guest"
        }",
    )

    fun reconfigureWorld(firstSetup: Boolean = false) = CompletableFuture
        .supplyAsync {
            Tasks.sync {
                destroyNPCEntities()
                destroyHologramEntities()

                val house = playerHouseReference

                if (house != null)
                {
                    house.houseNPCMap.values.forEach { npc ->
                        val entity = npc.toCubedNPC(bukkitWorld)

                        npcs[entity.location] = entity
                    }

                    house.houseHologramMap.values.forEach { hologram ->
                        val entity = hologram.toCubedHologram(bukkitWorld)

                        holograms[entity.location] = entity
                    }

                    onlinePlayers()
                        .forEach {
                            spawnEntities(it)
                        }
                }
            }
        }

    override fun onLogin(player: Player)
    {
        Tasks.delayed(10L) {
            if (playerHouseReference?.spawnPoint != null)
            {
                player.teleport(playerHouseReference!!.spawnPoint!!.toLocation(bukkitWorld))
            }

            spawnEntities(player)

            player.inventory.setItem(8, HousingItemService.realmItem)
            player.updateInventory()
        }
    }

    override fun playerResourcesOf(player: Player) = HousingPlayerResources(
        username = player.name,
        displayName = PlayerHandler.find(player.uniqueId)
            ?.getColoredName(prefixIncluded = true)
            ?: player.name,
        disguised = player.hasMetadata("disguised")
    )

    fun getHouse() = PlayerHousingService.findById(this.globalId)
}
