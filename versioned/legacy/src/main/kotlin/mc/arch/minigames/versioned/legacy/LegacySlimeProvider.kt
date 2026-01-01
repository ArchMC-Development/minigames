package mc.arch.minigames.versioned.legacy

import com.grinderwolf.swm.api.SlimePlugin
import com.grinderwolf.swm.nms.CraftSlimeWorld
import com.grinderwolf.swm.plugin.config.WorldData
import mc.arch.minigames.versioned.generics.SlimeProvider
import mc.arch.minigames.versioned.generics.SlimeWorldGeneric
import org.bukkit.Bukkit

/**
 * @author Subham
 * @since 8/5/25
 */
object LegacySlimeProvider : SlimeProvider
{
    private val slimePlugin = Bukkit.getServer().pluginManager
        .getPlugin("SlimeWorldManager") as SlimePlugin
    private val mongoLoader = slimePlugin.getLoader("mongodb")

    override fun queueGenerateWorld(worldGeneric: SlimeWorldGeneric<*>, newName: String)
    {
        val legacyWorld = worldGeneric as LegacySlimeWorld
        slimePlugin.generateWorld(
            legacyWorld.worldInstance.clone(newName)
        )
    }

    override fun loadReadOnlyWorld(name: String): SlimeWorldGeneric<*>
    {
        val worldData = WorldData()
        worldData.isPvp = true
        worldData.difficulty = "normal"
        worldData.environment = "NORMAL"
        worldData.worldType = "DEFAULT"

        worldData.isAllowAnimals = false
        worldData.isAllowMonsters = false

        return LegacySlimeWorld(
            worldInstance = slimePlugin
                .loadWorld(
                    mongoLoader,
                    name,
                    true,
                    worldData.toPropertyMap()
                )
        )
    }

    override fun loadPersistentHostedWorld(name: String): SlimeWorldGeneric<*>
    {
        val worldData = WorldData()
        worldData.isPvp = true
        worldData.difficulty = "normal"
        worldData.environment = "NORMAL"
        worldData.worldType = "DEFAULT"

        worldData.isAllowAnimals = false
        worldData.isAllowMonsters = false

        return LegacySlimeWorld(
            worldInstance = slimePlugin
                .loadWorld(
                    LegacyGridFSContentProvider,
                    name,
                    true,
                    worldData.toPropertyMap()
                )
        )
    }

    override fun createEmptyHostedWorld(name: String): SlimeWorldGeneric<*>
    {
        val worldData = WorldData()
        worldData.isPvp = true
        worldData.difficulty = "normal"
        worldData.environment = "NORMAL"
        worldData.worldType = "DEFAULT"

        worldData.isAllowAnimals = false
        worldData.isAllowMonsters = false

        return LegacySlimeWorld(
            worldInstance = slimePlugin
                .createEmptyWorld(
                    LegacyGridFSContentProvider,
                    name,
                    true,
                    worldData.toPropertyMap()
                )
        )
    }

    override fun saveWorld(generic: SlimeWorldGeneric<*>)
    {
        val legacy = generic as LegacySlimeWorld
        val serialized = (legacy.worldInstance as CraftSlimeWorld).serialize()

        LegacyGridFSContentProvider.saveWorld(
            generic.worldInstance.name, serialized, true
        )
    }
}
