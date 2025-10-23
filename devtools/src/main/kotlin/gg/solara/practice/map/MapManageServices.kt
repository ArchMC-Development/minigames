package gg.solara.practice.map

import com.grinderwolf.swm.api.SlimePlugin
import com.grinderwolf.swm.api.loaders.SlimeLoader
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.solara.practice.PracticeDevTools
import net.kyori.adventure.platform.bukkit.BukkitAudiences

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

    lateinit var slimePlugin: SlimePlugin
    lateinit var loader: SlimeLoader

    @Configure
    fun configure()
    {
        slimePlugin = plugin.server.pluginManager
            .getPlugin("SlimeWorldManager") as SlimePlugin

        loader = slimePlugin.getLoader("mongodb")
    }
}
