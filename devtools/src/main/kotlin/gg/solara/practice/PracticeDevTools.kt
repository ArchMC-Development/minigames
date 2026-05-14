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
import net.evilblock.cubed.util.ServerVersion
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
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
        MojangProfileLookupLogFilter.install()
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
                sendWelcome(event.player)
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

    private fun sendWelcome(player: Player)
    {
        val isLegacyFleet = ServerVersion.getVersion().isOlderThan(ServerVersion.v1_9)
        val divider = "${CC.GRAY}${CC.STRIKE_THROUGH}${"-".repeat(53)}"

        player.sendMessage(divider)
        player.sendMessage("${CC.AQUA}${CC.BOLD}DEVTOOLS ${if (isLegacyFleet) "LEGACY" else "MODERN"} MAP EDITOR!")
        player.sendMessage("")
        player.sendMessage("${CC.YELLOW}Editing")
        player.sendMessage("   ${CC.AQUA}/editor ${CC.WHITE}: create new maps from schematics")
        player.sendMessage("   ${CC.AQUA}/mapeditor ${CC.WHITE}: edit existing slime-template maps")
        player.sendMessage("   ${CC.AQUA}/mapmanage ${CC.WHITE}: map metadata operations")
        player.sendMessage("   ${CC.AQUA}/worldcomponent ${CC.WHITE}: attach reusable world components")
        player.sendMessage("   ${CC.AQUA}/schemacage ${CC.WHITE}: skywars cage schema editor")
        player.sendMessage("   ${CC.AQUA}/signlist [content]${CC.WHITE}: list signs in the current world")
        player.sendMessage("")
        player.sendMessage("${CC.YELLOW}Migration")
        if (isLegacyFleet)
        {
            player.sendMessage("   ${CC.AQUA}/maplegacyimport <name> ${CC.WHITE}: finish a modern→legacy conversion")
            player.sendMessage("   ${CC.AQUA}/mapmodernconvert <name> ${CC.WHITE}: start a legacy→modern conversion")
        }
        else
        {
            player.sendMessage("   ${CC.AQUA}/maplegacyconvert <name> ${CC.WHITE}: start a modern→legacy conversion")
            player.sendMessage("   ${CC.AQUA}/mapmodernimport <name> ${CC.WHITE}: finish a legacy→modern conversion")
        }
        player.sendMessage(divider)
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
