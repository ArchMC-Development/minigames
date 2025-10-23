package gg.solara.practice.editor.mapeditor

import com.grinderwolf.swm.api.loaders.SlimeLoader
import com.grinderwolf.swm.plugin.config.WorldData
import gg.scala.commons.spatial.Position
import gg.scala.lemon.hotbar.HotbarPreset
import gg.scala.lemon.hotbar.HotbarPresetHandler
import gg.scala.lemon.hotbar.entry.impl.DynamicHotbarPresetEntry
import gg.scala.lemon.hotbar.entry.impl.StaticHotbarPresetEntry
import gg.solara.practice.editor.SyntheticSignHologram
import gg.solara.practice.editor.SyntheticsEditor
import gg.solara.practice.editor.toBounds
import gg.solara.practice.map.MapManageServices
import gg.solara.practice.map.MapManageServices.slimePlugin
import gg.tropic.practice.map.MapService
import gg.tropic.practice.map.metadata.impl.MapSpawnMetadata
import gg.tropic.practice.map.metadata.scanner.AbstractMapMetadataScanner
import gg.tropic.practice.map.metadata.scanner.MetadataScannerUtilities
import gg.tropic.practice.map.metadata.sign.MapSignMetadataModel
import gg.tropic.practice.map.metadata.toSanitizedCoordinate
import gg.tropic.practice.map.synthetics.PreparedSyntheticSignModel
import gg.tropic.practice.map.utilities.MapMetadataScanUtilities
import me.lucko.helper.Events
import me.lucko.helper.Schedulers
import me.lucko.helper.terminable.composite.CompositeTerminable
import net.evilblock.cubed.entity.EntityHandler
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.LinkedList
import java.util.UUID

/**
 * @author GrowlyX
 * @since 7/18/2024
 */
class MapEditor(private val player: Player, private val loader: SlimeLoader) : SyntheticsEditor
{
    companion object
    {
        val instances = mutableMapOf<UUID, MapEditor>()
    }

    private val editorID = "editor-${player.uniqueId}"

    private var visiting: MapEditorInstance? = null
    private val schematics = loader.listWorlds()

    val terminable = CompositeTerminable.create()

    var isEditingSynthetic = false
    var syntheticScanner: AbstractMapMetadataScanner<*>? = null
    override val synthetics = mutableListOf<PreparedSyntheticSignModel>()

    fun prepareSyntheticMeta(input: String)
    {
        val lines = input.split(", ")
        val linked = LinkedList(lines)

        val hologram = SyntheticSignHologram(
            editor = this,
            refID = UUID.randomUUID(),
            playerLocation = player.location,
            type = syntheticScanner!!.type,
            extraLines = lines.toMutableList()
        )
        hologram.initializeData()
        EntityHandler.trackEntity(hologram)
        hologram.updateLines(hologram.getLines())
        synthetics += PreparedSyntheticSignModel(
            hologramEntity = hologram,
            signMetadataModel = MapSignMetadataModel(
                metaType = syntheticScanner!!.type,
                location = Position(
                    player.location.x.toSanitizedCoordinate(),
                    player.location.y.toSanitizedCoordinate(),
                    player.location.z.toSanitizedCoordinate(),
                    player.location.yaw,
                    player.location.pitch
                ),
                id = if (syntheticScanner!!.isAllExtra()) "global" else linked.pop(),
                extraMetadata = linked
            ),
            refID = hologram.refID
        )

        player.sendMessage("${CC.AQUA}Saved synthetic metadata! ${synthetics.size} synthetics exist (${listOf(
            player.location.x.toSanitizedCoordinate(),
            player.location.y.toSanitizedCoordinate(),
            player.location.z.toSanitizedCoordinate()
        )})")
    }

    fun prepareSyntheticMetaForExisting(model: MapSignMetadataModel)
    {
        val hologram = SyntheticSignHologram(
            editor = this,
            refID = UUID.randomUUID(),
            playerLocation = model.location.toLocation(visiting!!.world),
            type = model.metaType,
            extraLines = model.extraMetadata.toMutableList()
        )

        hologram.initializeData()
        EntityHandler.trackEntity(hologram)
        hologram.updateLines(hologram.getLines())

        synthetics += PreparedSyntheticSignModel(
            hologramEntity = hologram,
            signMetadataModel = model,
            refID = hologram.refID
        )
    }

    fun initialize()
    {
        if (schematics.isEmpty())
        {
            player.sendMessage("${CC.RED}There are no maps to edit!")
            return
        }

        Events
            .subscribe(PlayerDropItemEvent::class.java)
            .filter { it.player.uniqueId == player.uniqueId }
            .handler {
                it.isCancelled = true

                if (it.player.isSneaking)
                {
                    isEditingSynthetic = !isEditingSynthetic
                    if (isEditingSynthetic)
                    {
                        player.sendMessage("${CC.GREEN}Entering synthetic editor")
                        player.sendMessage("${CC.GREEN}Select a type: ${
                            MetadataScannerUtilities.scanners
                                .joinToString(", ") { scanner -> scanner.type }
                        }")

                        InputPrompt()
                            .acceptInput { player, string ->
                                syntheticScanner = MetadataScannerUtilities.matches(string)
                                player.sendMessage("${CC.GREEN}ENABLED! ${
                                    if (syntheticScanner!!.isAllExtra()) "${CC.GRAY}(all extra)" else ""
                                }")
                                player.playSound(player.location, Sound.FIREWORK_BLAST, 1.0f, 1.0f)
                            }
                            .start(player)
                    } else
                    {
                        player.sendMessage("${CC.RED}Left synthetic editor")
                    }
                } else
                {
                    if (isEditingSynthetic && syntheticScanner != null)
                    {
                        player.sendMessage("${CC.GREEN}TRACKED! Enter lines comma-separated:")
                        player.playSound(player.location, Sound.NOTE_PLING, 1.0f, 1.0f)

                        InputPrompt()
                            .acceptInput { player, string ->
                                prepareSyntheticMeta(string)
                                player.playSound(player.location, Sound.FIREWORK_BLAST, 1.0f, 1.0f)
                            }
                            .start(player)
                    }
                }
            }
            .bindWith(terminable)

        Events
            .subscribe(PlayerQuitEvent::class.java)
            .handler {
                if (it.player.uniqueId == player.uniqueId)
                {
                    terminable.closeAndReportException()
                }
            }
            .bindWith(terminable)

        instances[player.uniqueId] = this

        terminable.with {
            val audiences = MapManageServices.audiences.player(player)
            audiences.sendActionBar(Component.empty())

            player.inventory.clear()
            player.updateInventory()

            player.teleport(
                Location(
                    Bukkit.getWorld("world"),
                    0.0, 100.0, 0.0
                )
            )

            HotbarPresetHandler.forget(editorID)
            instances.remove(player.uniqueId)
            visiting?.closeAndReportException()
        }

        Schedulers
            .sync()
            .runRepeating({ task ->
                if (terminable.isClosed)
                {
                    task.closeAndReportException()
                    return@runRepeating
                }

                val audiences = MapManageServices.audiences.player(player)
                val availableSchematics = schematics.size

                audiences.sendActionBar(
                    LegacyComponentSerializer
                        .legacySection()
                        .deserialize(
                            "${CC.BD_AQUA}Map Editor ${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${
                                "${CC.AQUA}Visiting: ${CC.WHITE}${visiting?.slimeWorldName ?: "N/A"}${
                                    if (visiting != null) "${CC.D_GRAY} (${schematics.indexOf(visiting?.slimeWorldName) + 1}/${schematics.size})" else ""
                                }"
                            } ${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${
                                "${CC.AQUA}Maps: ${CC.WHITE}$availableSchematics"
                            }"
                        )
                )
            }, 0L, 10L)
            .bindWith(terminable)

        populateInventoryHotbar()
        visit(schematics.first())
    }

    fun visit(template: String)
    {
        val previousInstance = visiting

        val worldData = WorldData()
        worldData.isPvp = true
        worldData.difficulty = "normal"
        worldData.environment = "NORMAL"
        worldData.worldType = "DEFAULT"

        worldData.isAllowAnimals = false
        worldData.isAllowMonsters = false

        val slimeWorld = slimePlugin
            .loadWorld(
                loader,
                template,
                false,
                worldData.toPropertyMap()
            )

        slimePlugin.generateWorld(slimeWorld)

        val newWorld = Bukkit.getWorld(template)
            ?: return run {
                player.sendMessage("${CC.RED}Failed...")
                player.playSound(player.location, Sound.NOTE_PLING, 1.0f, 0.2f)
            }

        val instance = MapEditorInstance(template, newWorld)
        val map = MapService.mapWithSlime(instance.slimeWorldName)
            ?: return run {
                player.sendMessage("${CC.RED}No map with that slime world!")
            }

        player.gameMode = GameMode.CREATIVE
        player.allowFlight = true
        player.isFlying = true
        player.teleport(
            Location(
                newWorld, 0.0, 96.0, 0.0, -35.0f, 50.0f
            )
        )

        player.sendMessage("${CC.GREEN}Visiting ${CC.WHITE}$template${CC.GREEN}...")
        player.playSound(player.location, Sound.NOTE_PLING, 1.0f, 1.0f)

        visiting = instance

        isEditingSynthetic = false
        syntheticScanner = null
        synthetics.clear()

        map.metadata.synthetic?.map {
            prepareSyntheticMetaForExisting(it)
        }
        previousInstance?.closeAndReportException()
    }

    private fun populateInventoryHotbar()
    {
        val hotbarPreset = HotbarPreset()
        hotbarPreset.addSlot(0, DynamicHotbarPresetEntry().apply {
            onBuild = {
                ItemBuilder.of(Material.MELON)
                    .glow()
                    .name("${CC.RED}Visit Previous ${CC.GRAY}(Right Click)")
                    .build()
            }

            onClick = context@{
                if (visiting == null)
                {
                    player.sendMessage("${CC.RED}You are not visiting!")
                    return@context
                }

                val index = schematics.indexOf(visiting!!.slimeWorldName)
                visit(schematics.getOrNull(index - 1) ?: schematics.last())
            }
        })

        hotbarPreset.addSlot(8, DynamicHotbarPresetEntry().apply {
            onBuild = {
                ItemBuilder.of(Material.SPECKLED_MELON)
                    .glow()
                    .name("${CC.GREEN}Visit Next ${CC.GRAY}(Right Click)")
                    .build()
            }

            onClick = context@{
                if (visiting == null)
                {
                    player.sendMessage("${CC.RED}You are not visiting!")
                    return@context
                }

                val index = schematics.indexOf(visiting!!.slimeWorldName)
                visit(schematics.getOrNull(index + 1) ?: schematics.first())
            }
        })

        hotbarPreset.addSlot(7, DynamicHotbarPresetEntry().apply {
            onBuild = {
                ItemBuilder.of(Material.NAME_TAG)
                    .glow()
                    .name("${CC.YELLOW}Search ${CC.GRAY}(Right Click)")
                    .build()
            }

            onClick = context@{
                InputPrompt()
                    .withText("${CC.B_GOLD}Enter the schematic name!")
                    .acceptInput { _, schematic ->
                        val file = schematics.firstOrNull { it.equals(schematic, true) }
                            ?: return@acceptInput run {
                                player.sendMessage("${CC.RED}No schematic with that name!")
                            }

                        visit(file)
                    }
                    .start(player)
            }
        })

        hotbarPreset.addSlot(6, DynamicHotbarPresetEntry().apply {
            onBuild = {
                ItemBuilder.of(Material.PAINTING)
                    .glow()
                    .name("${CC.GOLD}All Schematics ${CC.GRAY}(Right Click)")
                    .build()
            }

            onClick = context@{
                object : PaginatedMenu()
                {
                    override fun getPrePaginatedTitle(player: Player) = "All schematics..."
                    override fun getAllPagesButtons(player: Player): Map<Int, Button>
                    {
                        val mappings = mutableMapOf<Int, Button>()
                        schematics.forEach {
                            mappings[mappings.size] = ItemBuilder
                                .of(Material.MAP)
                                .name("${CC.WHITE}$it")
                                .addToLore(
                                    "${CC.GREEN}Click to visit!"
                                )
                                .toButton { _, _ ->
                                    visit(it)
                                }
                        }

                        return mappings
                    }
                }.openMenu(player)
            }
        })

        hotbarPreset.addSlot(4, DynamicHotbarPresetEntry().apply {
            onBuild = {
                ItemBuilder.of(Material.GOLD_AXE)
                    .glow()
                    .name("${CC.B_YELLOW}Through ${CC.GRAY}(Right Click)")
                    .build()
            }

            onClick = context@{
                player.chat("//thru")
            }
        })

        hotbarPreset.addSlot(
            2, StaticHotbarPresetEntry(
                ItemBuilder.of(Material.SIGN)
                    .name("${CC.WHITE}Metadata Sign")
            )
        )

        hotbarPreset.addSlot(1, DynamicHotbarPresetEntry().apply {
            onBuild = {
                ItemBuilder.of(Material.ENDER_PORTAL_FRAME)
                    .glow()
                    .name("${CC.U_AQUA}Deploy Map ${CC.GRAY}(Right Click)")
                    .build()
            }

            onClick = context@{
                if (visiting == null)
                {
                    player.sendMessage("${CC.RED}You are not visiting!")
                    return@context
                }

                visiting!!.world.save()

                with(MapService.cached()) {
                    MapService.sync(this)
                    player.sendMessage("${CC.GREEN}Saved world!")
                }
            }
        })

        hotbarPreset.addSlot(3, DynamicHotbarPresetEntry().apply {
            onBuild = {
                ItemBuilder.of(Material.ACTIVATOR_RAIL)
                    .glow()
                    .name("${CC.D_RED}Update Map Metadata ${CC.GRAY}(Right Click)")
                    .build()
            }

            onClick = context@{
                if (visiting == null)
                {
                    player.sendMessage("${CC.RED}You are not visiting!")
                    return@context
                }

                val metadata = MapMetadataScanUtilities.buildMetadataFor(visiting!!.world)
                metadata.synthetic = synthetics.map { it.signMetadataModel }

                val map = MapService.mapWithSlime(visiting!!.slimeWorldName)
                    ?: return@context run {
                        player.sendMessage("${CC.RED}No map with that name!")
                    }

                val availableSpawnMetadata = metadata.metadata.filterIsInstance<MapSpawnMetadata>()
                if (availableSpawnMetadata.size >= 2)
                {
                    map.metadata = metadata

                    with(MapService.cached()) {
                        maps[map.name] = map
                        MapService.sync(this)

                        player.playSound(player.location, Sound.FIREWORK_LAUNCH, 1.0f, 1.0f)
                        player.sendMessage("${CC.B_GREEN}(!)${CC.GREEN} Updated map ${CC.YELLOW}${map.name}${CC.GREEN}!")
                    }
                } else
                {
                    player.sendMessage("${CC.RED}Your metadata is invalid!")
                }
            }
        })

        hotbarPreset.addSlot(5, DynamicHotbarPresetEntry().apply {
            onBuild = {
                ItemBuilder.of(Material.PAPER)
                    .glow()
                    .name("${CC.AQUA}Report Metadata ${CC.GRAY}(Right Click)")
                    .build()
            }

            onClick = context@{
                if (visiting == null)
                {
                    player.sendMessage("${CC.RED}You are not visiting!")
                    return@context
                }

                val metadata = MapMetadataScanUtilities.buildMetadataFor(visiting!!.world)
                player.sendMessage("${CC.GREEN}[Metadata report ${CC.GRAY}(${metadata.metadata.size})${CC.GREEN}]")
                metadata.metadata.forEach {
                    player.sendMessage("${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.WHITE}${it.report()}")
                }

                val synthetics = MapMetadataScanUtilities.buildJustInTimeMetadata(synthetics.map { it.signMetadataModel }, visiting!!.world)
                player.sendMessage("${CC.AQUA}[Synthetics report ${CC.GRAY}(${synthetics.size})${CC.AQUA}]")
                synthetics.forEach {
                    player.sendMessage("${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.WHITE}${it.report()}")
                }
            }
        })

        hotbarPreset.applyToPlayer(player)
        HotbarPresetHandler.startTrackingHotbar(
            editorID,
            hotbarPreset
        )
    }

}
