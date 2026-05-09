package mc.arch.minigames.versioned.modern

import mc.arch.minigames.versioned.generics.ItemStackProvider
import mc.arch.minigames.versioned.generics.VersionedProviders

/**
 * @author Subham
 * @since 8/5/25
 */
object ModernVersionedProviders : VersionedProviders
{
    override fun getSlimeProvider() = ModernSlimeProvider
    override fun getPotionProvider() = ModernPotionProvider
    override fun getPlayerProvider() = ModernPlayerProvider
    override fun getServerProvider() = ModernServerProvider
    override fun getKnockbackProvider() = ModernKnockbackProvider
    override fun getWorldProvider() = ModernWorldProvider
    override fun getSchematicProvider() = ModernSchematicProvider
    override fun getItemStackProvider() = ModernItemStackProvider
}
