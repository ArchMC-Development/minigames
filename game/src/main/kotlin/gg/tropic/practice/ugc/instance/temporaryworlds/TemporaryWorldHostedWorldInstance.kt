package gg.tropic.practice.ugc.instance.temporaryworlds

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.handler.PlayerHandler
import gg.tropic.practice.schematics.manipulation.BlockChanger
import gg.tropic.practice.extensions.getCylinderBlocks
import gg.tropic.practice.extensions.hypixelSpectatorCylinderHeight
import gg.tropic.practice.extensions.hypixelSpectatorCylinderRadius
import gg.tropic.practice.ugc.WorldInstanceProviderType
import gg.tropic.practice.ugc.generation.visits.VisitWorldRequest
import gg.tropic.practice.ugc.instance.BaseHostedWorldInstance
import gg.tropic.practice.ugc.temporaryworlds.TemporaryWorldPlayerResourcesWorld
import gg.tropic.practice.ugc.temporaryworlds.TemporaryWorldVisitConfiguration
import mc.arch.minigames.versioned.generics.worlds.LoadedSlimeWorld
import me.lucko.helper.Schedulers
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

/**
 * @author Subham
 * @since 7/20/25
 */
class TemporaryWorldHostedWorldInstance(
    private val request: VisitWorldRequest,
    override val loadedWorld: LoadedSlimeWorld
) : BaseHostedWorldInstance<TemporaryWorldPlayerResourcesWorld>(
    ownerPlayerId = request.ownerPlayerId,
    loadedWorld = loadedWorld,
    providerType = WorldInstanceProviderType.TEMPORARY_WORLD,
    persistence = null,
    globalId = request.worldGlobalId
)
{
    override fun onLoad()
    {
        val configuration = request.configuration as TemporaryWorldVisitConfiguration
        val baseBlock = XMaterial.valueOf(configuration.blockType).parseMaterial()!!

        // Little programmatically generated spawn location for the players lol
        // Disable lighting to save >90ms/tick during game starts
        bukkitWorld.custom().worldConfig.FEATURES_LIGHTING_ENABLED = false

        CompletableFuture
            .supplyAsync {
                getCylinderBlocks(
                    center = bukkitWorld.spawnLocation
                        .clone()
                        .subtract(0.0, 1.0, 0.0),
                    radius = hypixelSpectatorCylinderRadius,
                    height = hypixelSpectatorCylinderHeight
                ).map {
                    BlockChanger.BlockSnapshot(it, baseBlock)
                }.toSet() +                 getCylinderBlocks(
                    center = bukkitWorld.spawnLocation.clone(),
                    radius = hypixelSpectatorCylinderRadius - 1,
                    height = hypixelSpectatorCylinderHeight - 1
                ).map {
                    BlockChanger.BlockSnapshot(it, Material.AIR)
                }.toSet() + getCylinderBlocks(
                    center = bukkitWorld.spawnLocation
                        .clone()
                        .subtract(0.0, 1.0, 0.0),
                    radius = hypixelSpectatorCylinderRadius,
                    height = 1
                ).map {
                    BlockChanger.BlockSnapshot(it, baseBlock)
                }.toSet()
            }
            .thenComposeAsync { locations ->
                BlockChanger.setBlocksAsync(bukkitWorld, locations)
            }
            .thenRun {
                bukkitWorld.custom().worldConfig.FEATURES_LIGHTING_ENABLED = true
            }
            .exceptionally { throwable ->
                throwable.printStackTrace()
                return@exceptionally null
            }
            .join()
    }

    override fun playerResourcesOf(player: Player) = TemporaryWorldPlayerResourcesWorld(
        player.name,
        PlayerHandler.find(player.uniqueId)
            ?.getColoredName(prefixIncluded = true)
            ?: player.name,
        player.hasMetadata("disguised")
    )

    override fun onLogin(player: Player)
    {
        player.sendMessage("${CC.GREEN}Welcome to your Temporary World!")

        Schedulers
            .sync()
            .runLater({
                player.gameMode = GameMode.CREATIVE
                player.sendMessage("${CC.BD_GREEN}${Constants.CHECK_SYMBOL} ${CC.GRAY}You now have creative mode!")
            }, 10L)
    }

    override fun onUnload()
    {

    }
}
