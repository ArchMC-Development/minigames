package mc.arch.minigames.versioned.modern

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI
import com.infernalsuite.asp.api.world.properties.SlimeProperties
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap
import mc.arch.minigames.versioned.generics.SlimeProvider
import mc.arch.minigames.versioned.generics.SlimeWorldGeneric
import mc.arch.minigames.versioned.modern.custom.SpigotStoreMongoLoader
import me.lucko.helper.Schedulers
import net.kyori.adventure.nbt.LongBinaryTag
import org.bukkit.Bukkit

/**
 * @author Subham
 * @since 8/5/25
 */
object ModernSlimeProvider : SlimeProvider
{
    private val api = AdvancedSlimePaperAPI.instance()
    private val mongoDBLoader = SpigotStoreMongoLoader(
        "worlddb",
        "practice-worlds"
    )

    override fun queueGenerateWorld(worldGeneric: SlimeWorldGeneric<*>, newName: String)
    {
        val modern = worldGeneric as ModernSlimeWorld
        Schedulers
            .sync()
            .run {
                api.loadWorld(modern.worldInstance.clone(newName), true)
            }
            .apply {
                if (!Bukkit.isPrimaryThread())
                {
                    join()
                }
            }
    }

    override fun loadReadOnlyWorld(name: String): SlimeWorldGeneric<*>
    {
        return ModernSlimeWorld(
            worldInstance = api.readWorld(mongoDBLoader, name, true, SlimePropertyMap().apply {
                setValue(SlimeProperties.PVP, true)
                setValue(SlimeProperties.DIFFICULTY, "normal")
                setValue(SlimeProperties.ENVIRONMENT, "NORMAL")
                setValue(SlimeProperties.WORLD_TYPE, "DEFAULT")

                setValue(SlimeProperties.ALLOW_MONSTERS, false)
                setValue(SlimeProperties.ALLOW_ANIMALS, false)
            })
        )
    }

    override fun loadPersistentHostedWorld(name: String): SlimeWorldGeneric<*>
    {
        return ModernSlimeWorld(
            worldInstance = api.readWorld(ModernGridFSContentProvider, name, true, SlimePropertyMap().apply {
                setValue(SlimeProperties.PVP, true)
                setValue(SlimeProperties.DIFFICULTY, "normal")
                setValue(SlimeProperties.ENVIRONMENT, "NORMAL")
                setValue(SlimeProperties.WORLD_TYPE, "DEFAULT")

                setValue(SlimeProperties.ALLOW_MONSTERS, false)
                setValue(SlimeProperties.ALLOW_ANIMALS, false)
            })
        )
    }

    override fun createEmptyHostedWorld(name: String): SlimeWorldGeneric<*>
    {
        return ModernSlimeWorld(
            worldInstance = api.createEmptyWorld(name, true, SlimePropertyMap().apply {
                setValue(SlimeProperties.PVP, true)
                setValue(SlimeProperties.DIFFICULTY, "normal")
                setValue(SlimeProperties.ENVIRONMENT, "NORMAL")
                setValue(SlimeProperties.WORLD_TYPE, "DEFAULT")

                setValue(SlimeProperties.ALLOW_MONSTERS, false)
                setValue(SlimeProperties.ALLOW_ANIMALS, false)
            }, ModernGridFSContentProvider)
        )
    }

    override fun saveWorld(generic: SlimeWorldGeneric<*>)
    {
        val modern = generic.worldInstance as ModernSlimeWorld
        modern.updateLastSave()

        api.saveWorld(modern.worldInstance)
    }

    override fun importWorldFromBukkit(savedWorldFolder: java.io.File, newSlimeName: String)
    {
        // readVanillaWorld parses the vanilla folder into a SlimeWorld bound to the given
        // loader, but doesn't necessarily flush it — saveWorld is what actually writes the
        // bytes through the loader into mongo. Without this follow-up the import "succeeds"
        // and worldExists returns false moments later.
        val slime = api.readVanillaWorld(savedWorldFolder, newSlimeName, mongoDBLoader)
        api.saveWorld(slime)
    }

    override fun worldExists(name: String) = mongoDBLoader.worldExists(name)

    override fun listTemplates(): List<String> = mongoDBLoader.listWorlds()

    override fun deleteTemplate(name: String)
    {
        if (mongoDBLoader.worldExists(name)) mongoDBLoader.deleteWorld(name)
    }

    override fun versionOf(name: String): Int? = runCatching {
        // Slime format: 2-byte magic (0xB1 0x0B) + 1-byte version.
        val bytes = mongoDBLoader.readWorld(name) ?: return null
        if (bytes.size < 3) null else bytes[2].toInt() and 0xFF
    }.getOrNull()

    override fun loadAndRegisterTemplate(name: String, readOnly: Boolean)
    {
        val slime = api.readWorld(mongoDBLoader, name, readOnly, SlimePropertyMap().apply {
            setValue(SlimeProperties.PVP, true)
            setValue(SlimeProperties.DIFFICULTY, "normal")
            setValue(SlimeProperties.ENVIRONMENT, "NORMAL")
            setValue(SlimeProperties.WORLD_TYPE, "DEFAULT")

            setValue(SlimeProperties.ALLOW_MONSTERS, false)
            setValue(SlimeProperties.ALLOW_ANIMALS, false)
        })

        api.loadWorld(slime, true)

        // Force-materialise chunks for legacy slimes only — ASP keeps them lazy and
        // `world.loadedChunks` would otherwise come back empty for the metadata scanner.
        // Modern slimes are skipped: iterating ASP's chunkStorage while triggering
        // Bukkit chunk loads NPEs in the fastutil map iterator (concurrent mutation).
        val loaded = api.getLoadedWorld(name) ?: return
        if (loaded.dataVersion >= 1500) return

        val bukkitWorld = Bukkit.getWorld(name) ?: return
        loaded.chunkStorage.forEach { sc ->
            bukkitWorld.getChunkAt(sc.x, sc.z).load(true)
        }
    }
}
