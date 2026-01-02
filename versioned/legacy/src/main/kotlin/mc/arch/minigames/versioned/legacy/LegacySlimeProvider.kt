package mc.arch.minigames.versioned.legacy

import com.grinderwolf.swm.api.SlimePlugin
import com.grinderwolf.swm.nms.v1_8_R3.CustomWorldServer
import com.grinderwolf.swm.plugin.config.WorldData
import mc.arch.minigames.versioned.generics.SlimeProvider
import mc.arch.minigames.versioned.generics.SlimeWorldGeneric
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld

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

    fun queueGenerateWorldUncloned(worldGeneric: SlimeWorldGeneric<*>)
    {
        val legacyWorld = worldGeneric as LegacySlimeWorld
        slimePlugin.generateWorld(
            legacyWorld.worldInstance
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
                    false,
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
                    false,
                    worldData.toPropertyMap()
                )
        )
    }

    override fun saveWorld(generic: SlimeWorldGeneric<*>)
    {
        val legacy = generic as LegacySlimeWorld
        val worldName = legacy.worldInstance.name
        val bukkitWorld = Bukkit.getWorld(worldName)

        if (bukkitWorld != null)
        {
            val craftWorld = bukkitWorld as CraftWorld

            if (craftWorld.getHandle() !is CustomWorldServer)
            {
                println("NOT A CUSTOM WORLD SERVER")
                return
            }

            val worldServer: CustomWorldServer = craftWorld.handle as CustomWorldServer

            Bukkit.unloadWorld(bukkitWorld, true)
            val serialized = worldServer.slimeWorld.serialize()

            println("Serialized ${serialized.size} bytes")

            LegacyGridFSContentProvider.saveWorld(
                worldName, serialized, false
            )
        }
    }
}
