package gg.solara.practice.editor

import com.cryptomorin.xseries.XMaterial
import gg.scala.commons.spatial.Position
import gg.scala.commons.spatial.plus
import gg.scala.lemon.hotbar.HotbarPreset
import gg.scala.lemon.hotbar.HotbarPresetHandler
import gg.scala.lemon.hotbar.entry.impl.DynamicHotbarPresetEntry
import gg.scala.lemon.hotbar.entry.impl.StaticHotbarPresetEntry
import gg.solara.practice.map.MapManageServices
import gg.tropic.practice.map.MapService
import gg.tropic.practice.map.metadata.impl.MapSpawnMetadata
import gg.tropic.practice.map.utilities.MapMetadataScanUtilities
import gg.tropic.practice.map.metadata.scanner.AbstractMapMetadataScanner
import gg.tropic.practice.map.metadata.scanner.MetadataScannerUtilities
import gg.tropic.practice.map.metadata.sign.MapSignMetadataModel
import gg.tropic.practice.map.synthetics.PreparedSyntheticSignModel
import gg.tropic.practice.map.metadata.toSanitizedCoordinate
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
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.io.File
import java.util.LinkedList
import java.util.UUID

/**
 * @author GrowlyX
 * @since 7/18/2024
 */
class Editor(private val player: Player) : SyntheticsEditor
{
    companion object
    {
        val instances = mutableMapOf<UUID, Editor>()
    }

    private val containers = Containers()
    private val editorID = "editor-${player.uniqueId}"

    var readOnly = false

    var visiting: EditorInstance? = null
    private val schematics = (containers.schematicsDirectory.listFiles()
        ?.filter { it.isFile }
        ?.toList()?.filterNotNull() ?: emptyList()).map { SchematicEditable(it) } +
        (containers.devToolsWorldsDirectory.listFiles()
            ?.filter { it.isFile }
            ?.toList()?.filterNotNull() ?: emptyList()).map { WorldEditable(it) }

    val terminable = CompositeTerminable.create()

    var cloneTemplateSpawnID = ""
    var cloneTrackedMetadata = mutableListOf<MapSignMetadataModel>()

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

        player.sendMessage(
            "${CC.AQUA}Saved synthetic metadata! ${synthetics.size} synthetics exist (${
                listOf(
                    player.location.x.toSanitizedCoordinate(),
                    player.location.y.toSanitizedCoordinate(),
                    player.location.z.toSanitizedCoordinate()
                )
            })"
        )
    }

    /**
     * Converts a yaw value to a rotation in degrees (0, 90, 180, 270)
     * Uses BlockFace mappings for consistent yaw handling
     */
    private fun yawToRotation(yaw: Float): Int
    {
        // Convert yaw to BlockFace using MapBlockFaceUtilities
        val blockFace = gg.tropic.practice.map.utilities.MapBlockFaceUtilities.yawToFace(yaw, false)
            ?: return 0 // Default to 0 if null

        // Get the standardized yaw from SignParserUtilities.manualMappings
        val standardizedYaw = gg.tropic.practice.map.metadata.sign.manualMappings[blockFace] ?: 0f

        // Convert standardized yaw to rotation value (0, 90, 180, 270)
        return when (blockFace)
        {
            BlockFace.SOUTH -> 0    // 0 degrees
            BlockFace.WEST -> 90    // 90 degrees
            BlockFace.NORTH -> 180  // 180 degrees
            BlockFace.EAST -> 270   // 270 degrees
            else -> 0               // Default to 0 for other faces
        }
    }

    /**
     * Calculates the rotation needed to go from originalYaw to targetYaw
     * Returns the relative rotation in degrees (0, 90, 180, 270)
     */
    private fun yawToRotation(originalYaw: Float, targetYaw: Float): Int
    {
        // Convert yaws to BlockFaces
        val originalFace = gg.tropic.practice.map.utilities.MapBlockFaceUtilities.yawToFace(originalYaw, false)
            ?: return 0 // Default to 0 if null
        val targetFace = gg.tropic.practice.map.utilities.MapBlockFaceUtilities.yawToFace(targetYaw, false)
            ?: return 0 // Default to 0 if null

        // Get rotation values for both faces
        val originalRotation = when (originalFace)
        {
            BlockFace.SOUTH -> 0    // 0 degrees
            BlockFace.WEST -> 90    // 90 degrees
            BlockFace.NORTH -> 180  // 180 degrees
            BlockFace.EAST -> 270   // 270 degrees
            else -> 0               // Default to 0 for other faces
        }

        val targetRotation = when (targetFace)
        {
            BlockFace.SOUTH -> 0    // 0 degrees
            BlockFace.WEST -> 90    // 90 degrees
            BlockFace.NORTH -> 180  // 180 degrees
            BlockFace.EAST -> 270   // 270 degrees
            else -> 0               // Default to 0 for other faces
        }

        // Calculate relative rotation (how much to rotate from original to target)
        val relativeRotation = (targetRotation - originalRotation + 360) % 360

        // Normalize to 0, 90, 180, 270
        return when
        {
            relativeRotation < 45 -> 0
            relativeRotation < 135 -> 90
            relativeRotation < 225 -> 180
            relativeRotation < 315 -> 270
            else -> 0
        }
    }

    /**
     * Rotates a position by the given rotation in degrees
     */
    private fun rotatePosition(position: Position, rotation: Int): Position
    {
        return when (rotation)
        {
            0 -> position // No rotation
            90 -> Position(position.z, position.y, -position.x, position.yaw, position.pitch) // 90 degrees
            180 -> Position(-position.x, position.y, -position.z, position.yaw, position.pitch) // 180 degrees
            270 -> Position(-position.z, position.y, position.x, position.yaw, position.pitch) // 270 degrees
            else -> position // Default no rotation
        }
    }

    fun prepareSyntheticMetaBasedOnModelTemplate(
        spawn: Position,
        spawnID: String,
        model: MapSignMetadataModel,
        rotation: Int = 0
    )
    {
        // Rotate the model's location based on the rotation parameter
        val rotatedLocation = rotatePosition(model.location, rotation)
        val absolute = spawn + rotatedLocation
        val hologram = SyntheticSignHologram(
            editor = this,
            refID = UUID.randomUUID(),
            playerLocation = absolute
                .toLocation(visiting!!.world)
                .block.location
                .clone()
                .add(0.5, 0.0, 0.5),
            type = model.metaType,
            extraLines = (listOf(spawnID) + model.extraMetadata).toMutableList()
        )
        hologram.initializeData()
        EntityHandler.trackEntity(hologram)
        hologram.updateLines(hologram.getLines())

        synthetics += PreparedSyntheticSignModel(
            hologramEntity = hologram,
            signMetadataModel = MapSignMetadataModel(
                metaType = model.metaType,
                location = absolute,
                id = spawnID,
                extraMetadata = listOf() // doesn't support it, just set to nil
            ),
            refID = hologram.refID
        )
    }

    fun prepareSyntheticMetaBasedOnModelTemplate(
        spawn: Position,
        spawnID: String,
        model: MapSignMetadataModel,
        playerYaw: Float
    )
    {
        // Calculate rotation from player's yaw
        val rotation = yawToRotation(playerYaw)

        // Call the original method with the calculated rotation
        prepareSyntheticMetaBasedOnModelTemplate(spawn, spawnID, model, rotation)
    }

    fun prepareSyntheticMetaBasedOnModelTemplate(
        spawn: Position,
        spawnID: String,
        model: MapSignMetadataModel,
        originalYaw: Float,
        targetYaw: Float
    )
    {
        // Calculate relative rotation from original yaw to target yaw
        val rotation = yawToRotation(originalYaw, targetYaw)

        // Call the original method with the calculated rotation
        prepareSyntheticMetaBasedOnModelTemplate(spawn, spawnID, model, rotation)
    }

    fun initialize()
    {
        if (schematics.isEmpty())
        {
            player.sendMessage("${CC.RED}There are no schematics to edit!")
            return
        }

        var sneakTerminable: CompositeTerminable? = null
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
                        player.sendMessage(
                            "${CC.GREEN}Select a type: ${
                                MetadataScannerUtilities.scanners
                                    .joinToString(", ") { scanner -> scanner.type }
                            }")

                        InputPrompt()
                            .acceptInput { player, string ->
                                syntheticScanner = MetadataScannerUtilities.matches(string)
                                player.sendMessage(
                                    "${CC.GREEN}ENABLED! ${
                                        if (syntheticScanner!!.isAllExtra()) "${CC.GRAY}(all extra)" else ""
                                    }"
                                )
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

        /*Events
            .subscribe(PlayerToggleSneakEvent::class.java)
            .handler {
                if (it.player.uniqueId == player.uniqueId)
                {
                    if (it.isSneaking)
                    {
                        if (sneakTerminable != null)
                        {
                            return@handler
                        }

                        sneakTerminable = CompositeTerminable.create()

                        Schedulers
                            .sync()
                            .runRepeating({ _ ->
                                player.playSound(player.location, Sound.NOTE_PLING, 1.0f, 0.5f)
                            }, 0L, 5L)
                            .bindWith(sneakTerminable!!)

                        Schedulers
                            .sync()
                            .runLater({
                                val menu = object : Menu("Editor ${CC.GOLD}(EXPERIMENTS)")
                                {
                                    init
                                    {
                                        placeholder = true
                                    }

                                    override fun getButtons(player: Player) = mapOf(
                                        4 to ItemBuilder
                                            .of(Material.GLOWSTONE_DUST)
                                            .name("${CC.GOLD}(EXPERIMENT) ${CC.YELLOW}K-Means Island Clustering")
                                            .addToLore("${CC.GREEN}Click to start experiment.")
                                            .toButton { _, _ ->

                                            }
                                    )
                                }

                                menu.openMenu(player)

                                sneakTerminable?.closeAndReportException()
                                sneakTerminable = null
                            }, 20L)
                            .bindWith(sneakTerminable!!)
                    } else
                    {
                        sneakTerminable?.closeAndReportException()
                    }
                }
            }
            .bindWith(terminable)*/

        Events
            .subscribe(BlockBreakEvent::class.java)
            .filter { it.player.uniqueId == player.uniqueId }
            .handler {
                it.isCancelled = readOnly
            }
            .bindWith(terminable)

        Events
            .subscribe(BlockPlaceEvent::class.java)
            .filter { it.player.uniqueId == player.uniqueId }
            .handler {
                it.isCancelled = readOnly
            }
            .bindWith(terminable)

        Events
            .subscribe(PlayerQuitEvent::class.java)
            .handler {
                if (it.player.uniqueId == player.uniqueId)
                {
                    sneakTerminable?.closeAndReportException()
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
                            "${CC.BD_RED}Editor ${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${
                                "${CC.D_RED}Visiting: ${CC.WHITE}${visiting?.associatedEditable?.displayName ?: "N/A"}${
                                    if (visiting != null) "${CC.D_GRAY} (${schematics.indexOf(visiting?.associatedEditable) + 1}/${schematics.size})" else ""
                                }"
                            }"
                        )
                )
            }, 0L, 10L)
            .bindWith(terminable)

        populateInventoryHotbar()
        visit(schematics.first())
    }

    fun isWorldPresentInDB(): Boolean
    {
        val newMapName = visiting!!.associatedEditable
            .displayName
            .replace("_", "")
            .replace(" ", "")
            .capitalize()

        return MapManageServices.loader.worldExists("Map${newMapName.capitalize()}")
    }

    fun visit(editable: Editable)
    {
        val previousInstance = visiting
        val newWorld = editable.prepareWorld(player)
        val instance = EditorInstance(editable, newWorld)
        instance.bindWith(terminable)

        player.gameMode = GameMode.CREATIVE
        player.allowFlight = true
        player.isFlying = true
        player.teleport(
            Location(
                newWorld, 0.0, 96.0, 0.0, -65.0f, 0.0f
            )
        )

        player.sendMessage("${CC.GREEN}Visiting ${CC.WHITE}${editable.displayName}${CC.GREEN}...")

        player.playSound(player.location, Sound.NOTE_PLING, 1.0f, 1.0f)

        syntheticScanner = null
        synthetics.clear()
        isEditingSynthetic = false
        cloneTemplateSpawnID = ""
        cloneTrackedMetadata.clear()
        visiting = instance

        if (isWorldPresentInDB())
        {
            player.sendMessage("${CC.B_RED}${Constants.X_SYMBOL} ${CC.RED}Caution: This world was already developed by another user, and is in the database.")
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

                val index = schematics.indexOf(visiting!!.associatedEditable)
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

                val index = schematics.indexOf(visiting!!.associatedEditable)
                visit(schematics.getOrNull(index + 1) ?: schematics.first())
            }
        })

        hotbarPreset.addSlot(7, DynamicHotbarPresetEntry().apply {
            onBuild = {
                ItemBuilder.of(Material.NAME_TAG)
                    .glow()
                    .name("${CC.YELLOW}Read Only ${CC.GRAY}(Right Click)")
                    .build()
            }

            onClick = context@{
                readOnly = !readOnly
                it.sendMessage("${CC.GREEN}Read Only: $readOnly")
            }
        })

        hotbarPreset.addSlot(6, DynamicHotbarPresetEntry().apply {
            onBuild = {
                ItemBuilder.of(Material.PAINTING)
                    .glow()
                    .name("${CC.GOLD}All Editables ${CC.GRAY}(Right Click)")
                    .build()
            }

            onClick = context@{
                object : PaginatedMenu()
                {
                    var state: Boolean? = null

                    override fun getPrePaginatedTitle(player: Player) = "All editables..."
                    override fun size(buttons: Map<Int, Button>) = 54
                    override fun getMaxItemsPerPage(player: Player) = 45

                    override fun getGlobalButtons(player: Player) = mapOf(
                        4 to ItemBuilder
                            .of(XMaterial.OAK_SIGN)
                            .name(
                                "${CC.WHITE}Show state: ${
                                    when (state)
                                    {
                                        null -> "${CC.D_GREEN}All"
                                        true -> "${CC.GREEN}Complete"
                                        false -> "${CC.RED}Incomplete"
                                    }
                                }"
                            )
                            .toButton { _, _ ->
                                state = when (state)
                                {
                                    null -> true
                                    true -> false
                                    false -> null
                                }

                                Button.playNeutral(player)
                                openMenu(player)
                            }
                    )

                    override fun getAllPagesButtons(player: Player): Map<Int, Button>
                    {
                        val mappings = mutableMapOf<Int, Button>()
                        schematics
                            .filter {
                                when (state)
                                {
                                    null -> true
                                    true -> it.isComplete()
                                    false -> !it.isComplete()
                                }
                            }
                            .forEach {
                                mappings[mappings.size] = ItemBuilder
                                    .of(it.icon)
                                    .name("${CC.GREEN}${it.displayName}")
                                    .addToLore(
                                        "${CC.GRAY}Type: ${CC.WHITE}${it.javaClass.simpleName}",
                                        "${CC.GRAY}Status: ${
                                            if (it.isComplete())
                                            {
                                                "${CC.B_GREEN}${Constants.CHECK_SYMBOL} ${CC.GREEN}Complete"
                                            } else
                                            {
                                                "${CC.B_RED}${Constants.X_SYMBOL} ${CC.RED}Incomplete"
                                            }
                                        }",
                                        "",
                                        "${CC.GREEN}Click to visit!",
                                        "${CC.AQUA}Shift-Click to update status!",
                                    )
                                    .toButton { _, type ->
                                        if (type!!.isShiftClick)
                                        {
                                            if (it.isComplete())
                                            {
                                                it.markInComplete()
                                            } else
                                            {
                                                it.markComplete()
                                            }

                                            Button.playNeutral(player)
                                            openMenu(player)
                                            return@toButton
                                        }

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

                /*InputPrompt()
                    .withText("${CC.B_GOLD}Enter the map ID! ${CC.GRAY}(do not prefix with Map!)")
                    .acceptInput { _, newWorldName ->
                        prepareSlimeWorld(newWorldName)
                    }
                    .start(player)*/
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
                    .name("${CC.U_AQUA}Develop Map ${CC.GRAY}(Right Click)")
                    .build()
            }

            onClick = context@{
                if (visiting == null)
                {
                    player.sendMessage("${CC.RED}You are not visiting!")
                    return@context
                }

                player.sendMessage("${CC.GREEN}Prepared bounds!")

                val newMapName = visiting!!.associatedEditable
                    .displayName
                    .replace("_", "")
                    .replace(" ", "")
                    .capitalize()

                if (MapService.mapWithID(newMapName) != null)
                {
                    player.sendMessage("${CC.RED}A map with this name already exists! (Map System)")
                    return@context
                }

                if (MapManageServices.loader.worldExists("Map${newMapName.capitalize()}"))
                {
                    player.sendMessage("${CC.RED}A map with this name already exists! (MongoDB)")
                    return@context
                }

                val metadata = MapMetadataScanUtilities.buildMetadataFor(visiting!!.world)
                metadata.synthetic = synthetics.map(PreparedSyntheticSignModel::signMetadataModel)

                player.sendMessage("${CC.B_GRAY}(!)${CC.GRAY} Created a metadata copy! We're now going to build the map data model & store the map in MongoDB...")
                prepareSlimeWorld(newMapName)

                val map = gg.tropic.practice.map.Map(
                    name = newMapName.lowercase(),
                    metadata = metadata,
                    displayName = newMapName.capitalize(),
                    associatedSlimeTemplate = "Map${newMapName.capitalize()}"
                )

                with(MapService.cached()) {
                    maps[map.name] = map
                    MapService.sync(this)

                    player.playSound(player.location, Sound.FIREWORK_LAUNCH, 1.0f, 1.0f)
                    player.sendMessage("${CC.B_GREEN}(!)${CC.GREEN} Successfully created map ${CC.YELLOW}${map.name}${CC.GREEN}!")
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
                metadata.synthetic = synthetics.map(PreparedSyntheticSignModel::signMetadataModel)

                InputPrompt()
                    .withText("${CC.B_GOLD}Enter the map ID!")
                    .acceptInput { _, newMapName ->
                        val map = MapService.mapWithID(newMapName)
                            ?: return@acceptInput run {
                                player.sendMessage("${CC.RED}No schematic with that name!")
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
                    .start(player)
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
                metadata.synthetic = synthetics.map(PreparedSyntheticSignModel::signMetadataModel)

                player.sendMessage("${CC.GREEN}[Metadata report ${CC.GRAY}(${metadata.metadata.size})${CC.GREEN}]")
                metadata.metadata.forEach {
                    player.sendMessage("${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.WHITE}${it.report()}")
                }

                val synthetics = MapMetadataScanUtilities.buildJustInTimeMetadata(
                    synthetics.map { it.signMetadataModel },
                    visiting!!.world
                )
                player.sendMessage("${CC.AQUA}[Synthetics report ${CC.GRAY}(${metadata.metadata.size})${CC.AQUA}]")
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

    fun prepareSlimeWorld(newWorldName: String)
    {
        player.sendMessage("${CC.GOLD}Preparing world...")

        visiting!!.world.players.forEach { player ->
            player.teleport(
                Location(
                    Bukkit.getWorld("world"),
                    0.0, 100.0, 0.0
                )
            )
            player.sendMessage("${CC.RED}You were teleported out in preparation of a world import.")
        }

        val worldName = visiting!!.world.name
        Bukkit.unloadWorld(visiting!!.world, true)

        player.sendMessage("${CC.GREEN}Importing into SWM -> MongoDB...")
        MapManageServices.slimePlugin.importWorld(
            File(Bukkit.getWorldContainer(), worldName),
            "Map${newWorldName.capitalize()}",
            MapManageServices.loader
        )

        player.playSound(player.location, Sound.LEVEL_UP, 1.0f, 1.0f)
        player.sendMessage("${CC.GREEN}Imported!")

        val oldSchematic = visiting?.associatedEditable
        visiting?.closeAndReportException()
        visiting = null

        if (oldSchematic != null)
        {
            visit(oldSchematic)
        }
    }
}
