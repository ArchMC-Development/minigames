package mc.arch.minigames.persistent.housing.game.instance

import gg.scala.lemon.handler.PlayerHandler
import gg.tropic.practice.ugc.WorldInstanceProviderType
import gg.tropic.practice.ugc.generation.visits.VisitWorldRequest
import gg.tropic.practice.ugc.instance.BaseHostedWorldInstance
import mc.arch.minigames.persistent.housing.api.VisitHouseConfiguration
import mc.arch.minigames.persistent.housing.game.resources.HousingPlayerResources
import mc.arch.minigames.versioned.generics.worlds.LoadedSlimeWorld
import me.lucko.helper.Schedulers
import net.evilblock.cubed.entity.hologram.HologramEntity
import net.evilblock.cubed.entity.npc.NpcEntity
import net.evilblock.cubed.util.CC
import org.bukkit.Location
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

    override fun onLoad()
    {
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

    override fun generateScoreboardTitle(player: Player) = "${CC.BD_RED}REALMS"
    override fun generateScoreboardLines(player: Player) = listOf<String>()

    fun reconfigureWorld(firstSetup: Boolean = false) = CompletableFuture
        .supplyAsync {
            destroyNPCEntities()
            destroyHologramEntities()
        }

    override fun onLogin(player: Player)
    {
        player.updateInventory()
    }

    override fun playerResourcesOf(player: Player) = HousingPlayerResources(
        player.name,
        PlayerHandler.find(player.uniqueId)
            ?.getColoredName(prefixIncluded = true)
            ?: player.name,
        player.hasMetadata("disguised")
    )
}
