package gg.tropic.practice.ugc.strategies

import gg.scala.lemon.util.SplitUtil
import gg.tropic.practice.map.MapReplicationService
import gg.tropic.practice.ugc.WorldInstanceProviderType
import gg.tropic.practice.versioned.Versioned
import mc.arch.minigames.versioned.generics.SlimeWorldGeneric
import mc.arch.minigames.versioned.generics.worlds.LoadedSlimeWorld
import mc.arch.minigames.versioned.legacy.LegacySlimeProvider
import org.bukkit.World
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * @author Subham
 * @since 7/20/25
 */
object SlimeWorldLoadStrategy
{
    fun loadPersistentWorld(
        providerType: WorldInstanceProviderType,
        persistentWorldId: UUID,
        defaultCreator: (String) -> SlimeWorldGeneric<*>
    ): CompletableFuture<LoadedSlimeWorld?>
    {
        val slimeWorld = runCatching {
            Versioned.toProvider()
                .getSlimeProvider()
                .loadPersistentHostedWorld(persistentWorldId.toString())
        }.getOrElse {
            defaultCreator.invoke(persistentWorldId.toString())
        }

        return MapReplicationService
            .runInWorldLoadSync {
                LegacySlimeProvider.queueGenerateWorldUncloned(slimeWorld)
            }
            .thenCompose {
                MapReplicationService
                    .submitWorldRequest(persistentWorldId.toString())
                    .thenApply {
                        it?.let { world ->
                            LoadedSlimeWorld(
                                bukkitWorld = world,
                                generic = slimeWorld
                            )
                        }
                    }
            }
            .exceptionally { throwable ->
                throwable.printStackTrace()
                return@exceptionally null
            }
    }

    fun createTemporaryWorld(
        providerType: WorldInstanceProviderType,
        worldId: UUID
    ): CompletableFuture<LoadedSlimeWorld?>
    {
        val instanceWorldId = "hostedworld_instance_${providerType.name.lowercase()}_${
            "temporary"
        }_${
            SplitUtil.splitUuid(worldId)
        }"

        val slimeWorld = Versioned.toProvider()
            .getSlimeProvider()
            .createEmptyHostedWorld("temporary_$worldId")

        return MapReplicationService
            .runInWorldLoadSync {
                Versioned.toProvider()
                    .getSlimeProvider()
                    .queueGenerateWorld(slimeWorld, instanceWorldId)
            }
            .thenCompose {
                MapReplicationService
                    .submitWorldRequest(instanceWorldId)
                    .thenApply {
                        it?.let { world ->
                            LoadedSlimeWorld(
                                bukkitWorld = world,
                                generic = slimeWorld
                            )
                        }
                    }
            }
            .exceptionally { throwable ->
                throwable.printStackTrace()
                return@exceptionally null
            }
    }
}
