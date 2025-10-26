package gg.solara.practice.command

import com.grinderwolf.swm.api.world.properties.SlimePropertyMap
import gg.scala.commons.acf.CommandHelp
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.*
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.annotations.commands.customizer.CommandManagerCustomizer
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.command.ScalaCommandManager
import gg.scala.commons.issuer.ScalaPlayer
import gg.tropic.practice.map.Map
import gg.solara.practice.map.MapManageServices
import gg.tropic.practice.map.MapService
import gg.scala.commons.spatial.Bounds
import gg.scala.commons.spatial.toPosition
import gg.tropic.practice.map.metadata.impl.MapSpawnMetadata
import gg.tropic.practice.map.utilities.MapMetadataScanUtilities
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import org.bukkit.Bukkit
import org.bukkit.Sound

/**
 * @author GrowlyX
 * @since 9/22/2023
 */
@AutoRegister
@CommandAlias("mapmanage")
@CommandPermission("op")
object MapManageCommands : ScalaCommand()
{
    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp)
    {
        help.showHelp()
    }

    @CommandManagerCustomizer
    fun customize(manager: ScalaCommandManager)
    {
        manager.commandCompletions.registerAsyncCompletion("slime-templates") {
            MapManageServices.loader.listWorlds()
        }
    }

    @Subcommand("manipulate")
    fun onManipulate(player: ScalaPlayer)
    {
        MapService.editAndSave {
            maps.values.forEach { map ->
                map.associatedKitGroups += "mw_main"
                map.associatedKitGroups -= "__default__"
            }
        }
    }

    @Subcommand("create")
    @CommandCompletion("@slime-templates")
    @Description("Creates a new map on a new Slime world template.")
    fun onCreate(player: ScalaPlayer, @Single slimeTemplate: String, @Single mapName: String, @Optional unsafe: Boolean?)
    {
        if (MapService.mapWithID(mapName) != null)
        {
            throw ConditionFailedException(
                "A map with the ID $mapName already exists."
            )
        }

        val world = runCatching {
            MapManageServices.slimePlugin.loadWorld(
                MapManageServices.loader,
                slimeTemplate,
                true,
                SlimePropertyMap()
            )
        }.getOrNull() ?: throw ConditionFailedException(
            "The world $slimeTemplate either does not exist or is a locked world due to write being enabled."
        )

        MapManageServices.slimePlugin.generateWorld(world)

        with(player.bukkit()) {
            val currentLocation = location
            val devToolsMap = Bukkit.getWorld(slimeTemplate)
            teleport(devToolsMap.spawnLocation)

            allowFlight = true
            isFlying = true

            val metadata = MapMetadataScanUtilities.buildMetadataFor(devToolsMap)

            var success = false
            if ((unsafe != null && unsafe) || metadata.metadata.filterIsInstance<MapSpawnMetadata>().size >= 2)
            {
                sendMessage("${CC.B_GRAY}(!)${CC.GRAY} Created a metadata copy! We're now going to build the map data model...")

                val map = Map(
                    name = mapName,
                    metadata = metadata,
                    displayName = mapName,
                    associatedSlimeTemplate = slimeTemplate
                )

                with(MapService.cached()) {
                    maps[map.name] = map
                    MapService.sync(this)

                    playSound(location, Sound.FIREWORK_LAUNCH, 1.0f, 1.0f)
                    sendMessage("${CC.B_GREEN}(!)${CC.GREEN} Successfully created map ${CC.YELLOW}${map.name}${CC.GREEN}!")
                }

                success = true
            } else
            {
                sendMessage("${CC.B_RED}(!) Failed to create a metadata copy! We found no spawn locations in the map. Please try again.")
            }

            teleport(currentLocation)
            Bukkit.unloadWorld(devToolsMap, false)

            if (success)
            {
                sendMessage("${CC.B_GOLD}(!) ${CC.GOLD}Unloaded the DTT world, you're all set! Please note that map changes (adding/deleting maps) won't propagate to the game servers immediately. A restart is required for the changes to take effect.")
            }
        }
    }
}
