package gg.solara.practice.command

import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.*
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.scala.commons.spatial.Position
import gg.scala.commons.spatial.minus
import gg.scala.commons.spatial.vectorSubtract
import gg.scala.flavor.inject.Inject
import gg.solara.practice.PracticeDevTools
import gg.solara.practice.editor.Editor
import gg.solara.practice.editor.mapeditor.MapEditor
import gg.solara.practice.editor.template.IslandTemplate
import gg.tropic.practice.map.metadata.impl.MapSpawnMetadata
import gg.tropic.practice.map.metadata.sign.MapSignMetadataModel
import gg.tropic.practice.map.metadata.sign.parseIntoMetadata
import gg.tropic.practice.map.utilities.MapMetadataScanUtilities
import net.evilblock.cubed.util.CC
import org.bukkit.block.Sign

/**
 * @author GrowlyX
 * @since 7/18/2024
 */
@AutoRegister
@CommandPermission("op")
@CommandAlias("editor")
object EditorCommand : ScalaCommand()
{
    @Inject
    lateinit var plugin: PracticeDevTools

    @Default
    fun default(player: ScalaPlayer)
    {
        if (Editor.instances[player.uniqueId] != null)
        {
            throw ConditionFailedException("Already in editor, do /editor leave!")
        }

        if (MapEditor.instances[player.uniqueId] != null)
        {
            throw ConditionFailedException("Already in map editor, do /mapeditor leave!")
        }

        player.sendMessage("${CC.GOLD}Entering the editor...")
        Editor(player.bukkit()).apply {
            terminable.bindWith(plugin)
        }.initialize()
    }

    @Subcommand("save")
    fun save(player: ScalaPlayer, worldName: String)
    {
        Editor.instances[player.uniqueId]?.prepareSlimeWorld(worldName)
    }

    @Subcommand("pasteclone")
    fun cloneFrom(player: ScalaPlayer)
    {
        Editor.instances[player.uniqueId]?.apply {
            if (cloneTemplateSpawnID == "" || cloneTrackedMetadata.isEmpty())
            {
                throw ConditionFailedException(
                    "There is nothing to paste from your clone!"
                )
            }

            val metadata = MapMetadataScanUtilities.buildMetadataFor(visiting!!.world)
            val templateSpawns = metadata.metadata
                .filterIsInstance<MapSpawnMetadata>()
                .filterNot { it.id == cloneTemplateSpawnID }

            if (templateSpawns.isEmpty())
            {
                throw ConditionFailedException("There are no template spawns!")
            }

            val templateSpawnOG = metadata.metadata
                .filterIsInstance<MapSpawnMetadata>()
                .first { it.id == cloneTemplateSpawnID }

            templateSpawns.forEach { metadata ->
                cloneTrackedMetadata.forEach { model ->
                    prepareSyntheticMetaBasedOnModelTemplate(
                        spawn = metadata.position,
                        spawnID = metadata.id,
                        model = model,
                        originalYaw = templateSpawnOG.position.yaw, // Original spawn's yaw
                        targetYaw = metadata.position.yaw // Target spawn's yaw
                    )
                }

                player.sendMessage("${CC.GOLD}Generated ${cloneTrackedMetadata.size} clones for ${metadata.id}")
            }
        }
    }

    @Subcommand("clone")
    fun cloneFrom(player: ScalaPlayer, spawnID: String)
    {
        Editor.instances[player.uniqueId]?.apply {
            val metadata = MapMetadataScanUtilities.buildMetadataFor(visiting!!.world, usedSynthetically = true)
            val spawnByID = metadata.metadata
                .filterIsInstance<MapSpawnMetadata>()
                .firstOrNull { it.id == spawnID }
                ?: return@apply run {
                    player.sendMessage("${CC.RED}No spawn found by id $spawnID")
                }

            player.sendMessage("${CC.GREEN}using spawn id $spawnID")
            cloneTemplateSpawnID = spawnID
            cloneTrackedMetadata += synthetics
                .filter {
                    if (it.signMetadataModel.metaType == "spawn")
                    {
                        return@filter false
                    }

                    if (it.signMetadataModel.id == "global")
                    {
                        // ignore is extra system for this purpose lol
                        return@filter it.signMetadataModel.extraMetadata.first() == spawnID
                    }

                    it.signMetadataModel.id == spawnID
                }
                .map {
                    val blockPosition = spawnByID.position.vectorSubtract(Position(0.5, 0.0, 0.5))
                    player.sendMessage("${CC.AQUA}Cloned template of: ${CC.WHITE}${it.signMetadataModel.metaType} ${it.signMetadataModel.extraMetadata}")
                    MapSignMetadataModel(
                        metaType = it.signMetadataModel.metaType,
                        location = it.signMetadataModel.location - blockPosition,
                        id = spawnID,
                        extraMetadata = listOf()
                    )
                }

            val blocks = visiting!!.world.loadedChunks
                .flatMap {
                    it.tileEntities.toList()
                }

            val modelMappings = blocks
                .filterIsInstance<Sign>()
                .mapNotNull {
                    it.lines.toList()
                        .parseIntoMetadata(it.location)
                }

            cloneTrackedMetadata += modelMappings
                .filter {
                    if (it.metaType == "spawn")
                    {
                        return@filter false
                    }

                    if (it.id == "global")
                    {
                        // ignore is extra system for this purpose lol
                        return@filter it.extraMetadata.first() == spawnID
                    }

                    it.id == spawnID
                }
                .map {
                    player.sendMessage("${CC.D_AQUA}Cloned template of: ${CC.WHITE}${it.metaType} ${it.extraMetadata}")
                    MapSignMetadataModel(
                        metaType = it.metaType,
                        location = it.location - spawnByID.position,
                        id = spawnID,
                        extraMetadata = listOf()
                    )
                }

            player.sendMessage("${CC.YELLOW}Successfully took clone of team's area of $spawnID")
        }
    }

    @Subcommand("leave")
    fun leave(player: ScalaPlayer)
    {
        if (Editor.instances[player.uniqueId] == null)
        {
            throw ConditionFailedException("Not in editor!")
        }

        Editor.instances[player.uniqueId]?.terminable?.closeAndReportException()
        Editor.instances.remove(player.uniqueId)

        player.sendMessage("${CC.RED}Left editor.")
    }
}
