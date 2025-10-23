package gg.tropic.practice.minigame

import gg.tropic.practice.expectation.GameExpectation
import gg.tropic.practice.extensions.getCylinderBlocks
import gg.tropic.practice.extensions.hypixelSpectatorCylinderHeight
import gg.tropic.practice.extensions.hypixelSpectatorCylinderRadius
import gg.tropic.practice.games.GameImpl
import gg.tropic.practice.games.GameState
import gg.tropic.practice.kit.Kit
import gg.tropic.practice.schematics.manipulation.BlockChanger
import me.lucko.helper.Schedulers
import net.evilblock.cubed.util.ServerVersion
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 8/23/2024
 */
abstract class AbstractMiniGameGameImpl<T : MiniGameConfiguration>(
    arenaWorld: World,
    val orchestrator: BasicMiniGameOrchestrator<T>,
    expectation: GameExpectation, kit: Kit
) : GameImpl(arenaWorld, expectation, kit)
{
    lateinit var tracker: MiniGameEventTracker
    lateinit var playerTracker: MiniGameDisconnectedPlayerTracker

    var privateGame = false

    var fastTracked = false
    var startCountDown = expectationModel.miniGameConfiguration?.startGameCountDown ?: 5
    var spectatorLocation: Location? = null

    fun spectatorLocation() = spectatorLocation ?: Location(
        arenaWorld, 0.0, 100.0, 0.0
    )

    abstract fun prepare(configuration: T): MiniGameLifecycle<T>
    override fun buildResources(): Boolean
    {
        val miniGameConfig = expectationModel.miniGameConfiguration
            ?: return false

        miniGameLifecycle = prepare(miniGameConfig as T)
        if (miniGameLifecycle == null)
        {
            return false
        }

        miniGameLifecycle!!.configure()
        spectatorLocation = map.findSpawnLocations()
            .firstOrNull { it.id == "spec" }
            ?.position
            ?.toLocation(arenaWorld)

        tracker = MiniGameEventTracker(miniGameLifecycle!!)
        tracker.subscribe()

        playerTracker = MiniGameDisconnectedPlayerTracker(miniGameLifecycle!!)
        playerTracker.subscribe()

        var lastTimeNonZeroPlayers = System.currentTimeMillis()
        var hasTriggeredClose = false
        Schedulers
            .async()
            .runRepeating({ task ->
                if (hasTriggeredClose)
                {
                    task.closeAndReportException()
                    return@runRepeating
                }

                if (state != GameState.Playing)
                {
                    if (toBukkitPlayers().filterNotNull().isEmpty())
                    {
                        if (System.currentTimeMillis() - lastTimeNonZeroPlayers >= 30_000L)
                        {
                            hasTriggeredClose = true
                            state = GameState.Completed
                            closeAndCleanup(kickPlayers = true)
                        }
                    } else
                    {
                        lastTimeNonZeroPlayers = System.currentTimeMillis()
                    }

                    return@runRepeating
                }
            }, 0L, 20L)
            .bindWith(this)

        initializeTeamObjectives()
        return super.buildResources()
    }

    fun destroyHypixelSpectatorLocation()
    {
        if (spectatorLocation != null)
        {
            // Disable lighting to save >90ms/tick during game starts
            if (ServerVersion.getVersion().isOlderThan(ServerVersion.v1_9))
            {
                arenaWorld.custom().worldConfig.FEATURES_LIGHTING_ENABLED = false
            }

            CompletableFuture
                .supplyAsync {
                    getCylinderBlocks(
                        center = spectatorLocation!!.clone().subtract(0.0, 3.0, 0.0),
                        radius = hypixelSpectatorCylinderRadius,
                        height = hypixelSpectatorCylinderHeight
                    ).map {
                        BlockChanger.BlockSnapshot(it, Material.AIR)
                    }.toSet()
                }
                .thenComposeAsync { locations ->
                    BlockChanger.setBlocksAsync(
                        arenaWorld,
                        locations
                    )
                }
                .thenRun {
                    if (ServerVersion.getVersion().isOlderThan(ServerVersion.v1_9))
                    {
                        arenaWorld.custom().worldConfig.FEATURES_LIGHTING_ENABLED = true
                    }
                }
                .exceptionally { throwable ->
                    throwable.printStackTrace()
                    return@exceptionally null
                }
        }
    }
}
