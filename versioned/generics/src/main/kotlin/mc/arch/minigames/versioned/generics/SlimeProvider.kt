package mc.arch.minigames.versioned.generics

interface SlimeProvider
{
    fun queueGenerateWorld(worldGeneric: SlimeWorldGeneric<*>, newName: String)
    fun loadReadOnlyWorld(name: String): SlimeWorldGeneric<*>

    fun loadPersistentHostedWorld(name: String): SlimeWorldGeneric<*>
    fun createEmptyHostedWorld(name: String): SlimeWorldGeneric<*>

    fun saveWorld(generic: SlimeWorldGeneric<*>)

    fun importWorldFromBukkit(savedWorldFolder: java.io.File, newSlimeName: String)

    fun worldExists(name: String): Boolean

    fun listTemplates(): List<String>
    fun loadAndRegisterTemplate(name: String, readOnly: Boolean)

    /** Slime format version byte (v9 = legacy SWM, v10+ = modern ASP). Caller-cached. */
    fun versionOf(name: String): Int?
}
