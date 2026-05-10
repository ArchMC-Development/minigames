package gg.solara.practice.map

import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Service
import gg.solara.practice.PracticeDevTools
import gg.tropic.practice.provider.MiniProviderVersion
import gg.tropic.practice.versioned.Versioned
import mc.arch.minigames.versioned.generics.SlimeProvider
import net.kyori.adventure.platform.bukkit.BukkitAudiences

@Service
object MapManageServices
{
    @Inject
    lateinit var plugin: PracticeDevTools

    @Inject
    lateinit var audiences: BukkitAudiences

    val slime: SlimeProvider
        get() = Versioned.toProvider().getSlimeProvider()

    fun detectVersion(slimeTemplate: String): MiniProviderVersion
    {
        val formatByte = slime.versionOf(slimeTemplate)
            ?: return if (net.evilblock.cubed.util.ServerVersion.getVersion()
                    .isOlderThan(net.evilblock.cubed.util.ServerVersion.v1_9))
                MiniProviderVersion.LEGACY
            else
                MiniProviderVersion.MODERN

        return if (formatByte <= 9) MiniProviderVersion.LEGACY else MiniProviderVersion.MODERN
    }
}
