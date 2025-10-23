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
}
