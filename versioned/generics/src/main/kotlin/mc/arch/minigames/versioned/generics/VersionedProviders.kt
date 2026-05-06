package mc.arch.minigames.versioned.generics

/**
 * @author Subham
 * @since 8/5/25
 */
interface VersionedProviders
{
    fun getSlimeProvider(): SlimeProvider
    fun getPotionProvider(): PotionProvider
    fun getPlayerProvider(): PlayerProvider
    fun getServerProvider(): ServerProvider
    fun getKnockbackProvider(): KnockbackProvider
    fun getWorldProvider(): WorldProvider
}
