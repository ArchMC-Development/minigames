package gg.solara.practice.map

import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Service
import gg.solara.practice.PracticeDevTools
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
}
