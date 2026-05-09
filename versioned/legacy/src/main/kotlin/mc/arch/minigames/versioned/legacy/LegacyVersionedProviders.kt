package mc.arch.minigames.versioned.legacy

import mc.arch.minigames.versioned.generics.VersionedProviders

/**
 * @author Subham
 * @since 8/5/25
 */
object LegacyVersionedProviders : VersionedProviders
{
    override fun getSlimeProvider() = LegacySlimeProvider
    override fun getPotionProvider() = LegacyPotionProvider
    override fun getPlayerProvider() = LegacyPlayerProvider
    override fun getServerProvider() = LegacyServerProvider
    override fun getKnockbackProvider() = LegacyKnockbackProvider
    override fun getWorldProvider() = LegacyWorldProvider
    override fun getSchematicProvider() = LegacySchematicProvider
}
