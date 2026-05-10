package gg.solara.practice.migration

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI
import com.mongodb.client.model.Filters
import gg.tropic.practice.versioned.Versioned
import me.lucko.helper.Schedulers
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.WorldType
import org.bukkit.block.Banner
import org.bukkit.block.CommandBlock
import org.bukkit.block.Container
import org.bukkit.block.CreatureSpawner
import org.bukkit.block.Sign
import org.bukkit.block.Skull
import org.bukkit.entity.Player
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.ChunkGenerator
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Random
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Modern → legacy export: walks a v13 slime via ASP, copies blocks + sign/container
 * state into a vanilla Anvil world, zips it, uploads to `UGC.legacy-pending-import`.
 * Finish on legacy with `/maplegacyimport`.
 */
object MapLegacyExporter
{
    private val bucket = pendingBucket(LEGACY_PENDING_BUCKET)

    fun convertAndUpload(player: Player, sourceName: String)
    {
        val provider = Versioned.toProvider().getSlimeProvider()

        val currentVersion = runCatching { provider.versionOf(sourceName) }.getOrNull()
        if (currentVersion != null && currentVersion <= 9)
        {
            player.sendMessage(
                "${CC.YELLOW}$sourceName is already legacy (slime v$currentVersion); no conversion needed."
            )
            return
        }

        player.sendMessage("${CC.YELLOW}Loading ${CC.WHITE}$sourceName${CC.YELLOW} read-only for export...")

        runCatching { provider.loadAndRegisterTemplate(sourceName, readOnly = true) }
            .onFailure {
                player.sendMessage("${CC.RED}Could not load $sourceName: ${it.message ?: it.javaClass.simpleName}")
                return
            }

        val source = Bukkit.getWorld(sourceName)
            ?: return fail(player, "World $sourceName did not register after load.")

        val targetName = "legacyexport_${sourceName}_${UUID.randomUUID().toString().take(8)}"
        val target = WorldCreator(targetName)
            .type(WorldType.FLAT)
            .generator(EmptyChunkGenerator)
            .createWorld()
            ?: return fail(player, "Failed to create vanilla export world.")

        val chunkCoords = AdvancedSlimePaperAPI.instance().getLoadedWorld(sourceName)
            ?.chunkStorage
            ?.map { it.x to it.z }
            ?: return fail(player, "ASP did not return a SlimeWorldInstance for $sourceName.")

        player.sendMessage("${CC.YELLOW}Copying ${chunkCoords.size} chunks (this blocks the main thread; please wait)...")

        // Run on main thread regardless of caller — Bukkit chunk + tile-entity reads
        // race off-thread and can hand back stale or partially-loaded data.
        val tileEntities = Schedulers.sync().supply {
            copyChunks(source, target, chunkCoords)
        }.join()

        player.sendMessage("${CC.YELLOW}Saved $tileEntities tile entities. Persisting world to disk...")

        Schedulers.sync().run {
            target.save()
            Bukkit.unloadWorld(target, true)
        }.join()

        val targetFolder = File(Bukkit.getWorldContainer(), targetName)
        if (!targetFolder.isDirectory)
        {
            return fail(player, "Vanilla export folder missing after unload — aborting.")
        }

        Schedulers.async().run {
            runCatching { uploadFolder(sourceName, targetFolder) }
                .onFailure { ex ->
                    Schedulers.sync().run {
                        player.sendMessage("${CC.RED}GridFS upload failed: ${ex.message}")
                    }
                }
                .onSuccess {
                    Schedulers.sync().run {
                        player.sendMessage("${CC.GREEN}Uploaded export for ${CC.WHITE}$sourceName${CC.GREEN}.")
                        player.sendMessage("${CC.GRAY}Run ${CC.YELLOW}/maplegacyimport $sourceName${CC.GRAY} on the legacy fleet to finish.")
                        targetFolder.deleteRecursively()
                    }
                }
        }
    }

    private fun copyChunks(source: World, target: World, coords: List<Pair<Int, Int>>): Int
    {
        // Use the source's actual height range — modern worlds extend down to -64 and
        // truncating at y=0 silently drops anything in cave/deepslate space.
        val minY = source.minHeight
        val maxY = source.maxHeight - 1
        var copiedTileEntities = 0

        for ((cx, cz) in coords)
        {
            val srcChunk = source.getChunkAt(cx, cz).also { it.load(true) }
            val tgtChunk = target.getChunkAt(cx, cz).also { it.load(true) }

            for (x in 0..15) for (z in 0..15) for (y in minY..maxY)
            {
                val src = srcChunk.getBlock(x, y, z)
                if (src.type != Material.AIR)
                {
                    tgtChunk.getBlock(x, y, z).blockData = src.blockData
                }
            }

            for (te in srcChunk.tileEntities)
            {
                val tgt = target.getBlockAt(te.x, te.y, te.z).state
                if (copyTileEntity(te, tgt)) copiedTileEntities++
            }
        }
        return copiedTileEntities
    }

    private fun copyTileEntity(src: org.bukkit.block.BlockState, tgt: org.bukkit.block.BlockState): Boolean
    {
        when
        {
            src is Sign && tgt is Sign ->
            {
                @Suppress("DEPRECATION")
                src.lines.forEachIndexed { i, line -> tgt.setLine(i, line) }
            }
            src is Container && tgt is Container ->
            {
                tgt.inventory.contents = src.inventory.contents
            }
            src is Banner && tgt is Banner ->
            {
                @Suppress("DEPRECATION")
                tgt.baseColor = src.baseColor
                tgt.patterns = src.patterns
            }
            src is CommandBlock && tgt is CommandBlock ->
            {
                tgt.setCommand(src.command)
                tgt.setName(src.name)
            }
            src is CreatureSpawner && tgt is CreatureSpawner ->
            {
                tgt.spawnedType = src.spawnedType
                tgt.delay = src.delay
            }
            src is Skull && tgt is Skull ->
            {
                @Suppress("DEPRECATION")
                tgt.owner = src.owner
            }
            else -> return false
        }
        tgt.update(true, false)
        return true
    }

    private fun uploadFolder(name: String, folder: File)
    {
        val payload = zipFolder(folder)
        bucket.find(Filters.eq("filename", name)).firstOrNull()?.let { bucket.delete(it.objectId) }
        bucket.openUploadStream(name).use { it.write(payload) }
    }

    private fun zipFolder(folder: File): ByteArray = ByteArrayOutputStream().use { baos ->
        ZipOutputStream(baos).use { zip ->
            // Both sides absolute or `relativize` rejects mixed paths when Bukkit's
            // world container is configured relatively.
            val basePath = folder.absoluteFile.toPath().normalize()
            folder.walkTopDown().filter { it.isFile }.forEach { file ->
                val rel = basePath.relativize(file.absoluteFile.toPath().normalize())
                    .toString()
                    .replace(File.separatorChar, '/')
                zip.putNextEntry(ZipEntry(rel))
                file.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
        baos.toByteArray()
    }

    private fun fail(player: Player, message: String)
    {
        player.sendMessage("${CC.RED}$message")
    }

    private object EmptyChunkGenerator : ChunkGenerator()
    {
        override fun canSpawn(world: World, x: Int, z: Int) = true
        override fun getDefaultPopulators(world: World): MutableList<BlockPopulator> = mutableListOf()
        override fun getFixedSpawnLocation(world: World, random: Random) =
            org.bukkit.Location(world, 0.0, 64.0, 0.0)
    }
}
