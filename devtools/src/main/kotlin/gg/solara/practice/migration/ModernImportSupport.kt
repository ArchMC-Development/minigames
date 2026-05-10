package gg.solara.practice.migration

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI
import com.infernalsuite.asp.api.loaders.SlimeLoader
import com.infernalsuite.asp.api.world.properties.SlimeProperties
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap
import mc.arch.minigames.versioned.modern.custom.SpigotStoreMongoLoader

/**
 * ASP-only half of the legacy → modern import. Kept in its own file so the legacy fleet
 * never links ASP classes — only invoked from [MapConversionCommands] behind its
 * `aspAvailable` gate.
 */
internal object ModernImportSupport
{
    private val api = AdvancedSlimePaperAPI.instance()
    private val mongoDBLoader = SpigotStoreMongoLoader("worlddb", "practice-worlds")

    fun importBytes(name: String, bytes: ByteArray)
    {
        val slime = api.readWorld(
            OneShotSlimeLoader(name, bytes),
            name,
            true,
            SlimePropertyMap().apply {
                setValue(SlimeProperties.PVP, true)
                setValue(SlimeProperties.DIFFICULTY, "normal")
                setValue(SlimeProperties.ENVIRONMENT, "NORMAL")
                setValue(SlimeProperties.WORLD_TYPE, "DEFAULT")
                setValue(SlimeProperties.ALLOW_MONSTERS, false)
                setValue(SlimeProperties.ALLOW_ANIMALS, false)
            }
        )

        // ASP refuses `clone(sameName, otherLoader)`, so route through a scratch entry
        // and copy bytes back to the real name. Two writes; the destination ends up v13.
        val scratch = "$name.modernimport.tmp"
        if (mongoDBLoader.worldExists(scratch)) mongoDBLoader.deleteWorld(scratch)
        slime.clone(scratch, mongoDBLoader)

        try
        {
            val v13Bytes = mongoDBLoader.readWorld(scratch)
            if (mongoDBLoader.worldExists(name)) mongoDBLoader.deleteWorld(name)
            mongoDBLoader.saveWorld(name, v13Bytes)
        }
        finally
        {
            runCatching { mongoDBLoader.deleteWorld(scratch) }
        }
    }
}

/** Single-world, read-only in-memory `SlimeLoader` used as a one-time source. */
private class OneShotSlimeLoader(
    private val name: String,
    private val bytes: ByteArray,
) : SlimeLoader
{
    override fun readWorld(worldName: String): ByteArray
    {
        require(worldName == name) { "OneShotSlimeLoader only knows '$name', asked for '$worldName'" }
        return bytes
    }

    override fun worldExists(worldName: String): Boolean = worldName == name
    override fun listWorlds(): MutableList<String> = mutableListOf(name)
    override fun saveWorld(worldName: String, serializedWorld: ByteArray) = Unit
    override fun deleteWorld(worldName: String) = Unit
}
