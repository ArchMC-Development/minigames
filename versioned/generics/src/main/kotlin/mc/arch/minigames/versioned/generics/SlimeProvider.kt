package mc.arch.minigames.versioned.generics

/**
 * @author Subham
 * @since 8/5/25
 */
interface SlimeProvider
{
    fun queueGenerateWorld(worldGeneric: SlimeWorldGeneric<*>, newName: String)
    fun loadReadOnlyWorld(name: String): SlimeWorldGeneric<*>

    fun loadPersistentHostedWorld(name: String): SlimeWorldGeneric<*>
    fun createEmptyHostedWorld(name: String): SlimeWorldGeneric<*>

    fun saveWorld(generic: SlimeWorldGeneric<*>)
}
