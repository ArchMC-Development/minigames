package mc.arch.minigames.versioned.generics

/**
 * @author Subham
 * @since 8/5/25
 */
interface SlimeWorldGeneric<W : Any>
{
    val worldInstance: W
    fun clone(newName: String): SlimeWorldGeneric<W>

    fun getLastSave(): Long?
    fun updateLastSave()
}
