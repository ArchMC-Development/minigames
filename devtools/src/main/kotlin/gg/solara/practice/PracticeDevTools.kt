package gg.solara.practice

import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.annotations.container.ContainerDisable
import gg.scala.commons.annotations.container.ContainerEnable
import gg.scala.commons.annotations.container.ContainerPreEnable
import gg.scala.commons.core.plugin.*
import gg.tropic.practice.PracticeShared
import gg.tropic.practice.schematics.manipulation.BlockChanger
import me.lucko.helper.Events
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.EntityType
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.player.PlayerJoinEvent
import kotlin.properties.Delegates

/**
 * @author GrowlyX
 * @since 9/22/2023
 */
@Plugin(
    name = "DevTools",
    version = "%remote%/%branch%/%id%"
)
@PluginAuthor("ArchMC")
@PluginWebsite("https://arch.mc")
@PluginDependencyComposite(
    PluginDependency("scala-commons"),
    PluginDependency("Lemon"),
    PluginDependency("WorldEdit"),
    PluginDependency("ScBasics", soft = true)
)
class PracticeDevTools : ExtendedScalaPlugin()
{
    companion object
    {
        @JvmStatic
        var instance by Delegates.notNull<PracticeDevTools>()
    }

    init
    {
        PracticeShared
    }

    @ContainerPreEnable
    fun preEnable()
    {
        instance = this
    }

    @ContainerEnable
    fun containerEnable()
    {
        BlockChanger.load(this, false)
        Bukkit.getWorldContainer().listFiles()
            ?.filter { it.name.startsWith("editor") }
            ?.forEach {
                it.deleteRecursively()
            }

        Events
            .subscribe(PlayerJoinEvent::class.java)
            .handler { event ->
                event.player.gameMode = GameMode.CREATIVE
                event.player.teleport(
                    event.player.location.clone().apply {
                        x = 0.0
                        y = 64.0
                        z = 0.0
                    }
                )
                event.player.sendMessage("${CC.GRAY}${CC.STRIKE_THROUGH}${"-".repeat(53)}")
                event.player.sendMessage("${CC.AQUA}DevTools: ${CC.WHITE}Prepare, edit, and view Minigame maps")
                event.player.sendMessage("   ${CC.AQUA}/editor: ${CC.YELLOW}Create new maps")
                event.player.sendMessage("   ${CC.AQUA}/mapeditor: ${CC.YELLOW}Edit existing maps")
                event.player.sendMessage("${CC.GRAY}${CC.STRIKE_THROUGH}${"-".repeat(53)}")
            }

        Events
            .subscribe(EntitySpawnEvent::class.java)
            .handler {
                if (it.entityType != EntityType.PLAYER)
                {
                    return@handler
                }

                it.isCancelled = true
            }
            .bindWith(this)
    }

    @ContainerDisable
    fun containerDisable()
    {
        Bukkit.getWorlds()
            .filter { it.name.startsWith("editor") }
            .forEach {
                Bukkit.unloadWorld(it, false)
            }
    }
}
