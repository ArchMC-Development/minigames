package gg.solara.practice.map

import com.grinderwolf.swm.api.SlimePlugin
import com.grinderwolf.swm.api.loaders.SlimeLoader
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Service
import gg.solara.practice.PracticeDevTools
import gg.tropic.practice.versioned.Versioned
import mc.arch.minigames.versioned.generics.SlimeProvider
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.Bukkit

/**
 * @author GrowlyX
 * @since 9/22/2023
 */
@Service
object MapManageServices
{
    @Inject
    lateinit var plugin: PracticeDevTools

    @Inject
    lateinit var audiences: BukkitAudiences

    val slime: SlimeProvider
        get() = Versioned.toProvider().getSlimeProvider()

    /**
     * Legacy SWM accessors. Only present on the 1.8 devtools fleet — calling these on the
     * Paper 1.21 modern-devtools fleet will throw because Grinderwolf SWM is not installed
     * there. Cross-version code paths must use [slime] instead.
     */
    val slimePlugin: SlimePlugin
        get() = Bukkit.getPluginManager()
            .getPlugin("SlimeWorldManager") as SlimePlugin

    val loader: SlimeLoader
        get() = slimePlugin.getLoader("mongodb")
}
