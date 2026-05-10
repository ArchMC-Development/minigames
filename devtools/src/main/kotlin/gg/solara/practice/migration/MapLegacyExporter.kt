package gg.solara.practice.migration

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI
import com.mongodb.client.gridfs.GridFSBuckets
import com.mongodb.client.gridfs.model.GridFSUploadOptions
import com.mongodb.client.model.Filters
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import gg.scala.store.ScalaDataStoreShared
import gg.tropic.practice.versioned.Versioned
import me.lucko.helper.Schedulers
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.WorldType
import org.bukkit.block.Container
import org.bukkit.block.Sign
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
 * Modern → legacy export: walk a v13 slime via ASP, copy blocks + sign/container state
 * into a vanilla Anvil world, zip the folder, and upload to `UGC.legacy-pending-import`.
 * The legacy fleet's `/maplegacyimport` finishes the round-trip.
 *
 * Tile-entity coverage is limited to Signs and Containers. Modern-only blocks lose
 * fidelity on legacy. Chunk copy runs on the main thread; large maps will pause it.
 */
object MapLegacyExporter
{
    private const val BUCKET_NAME = "legacy-pending-import"

    private val database = ScalaDataStoreShared.INSTANCE
        .getNewMongoConnection()
        .getConnection()
        .getDatabase("UGC")

    private val bucket = GridFSBuckets.create(database, BUCKET_NAME)

    init
    {
        database.getCollection("$BUCKET_NAME.files")
            .createIndex(
                Indexes.ascending("filename"),
                IndexOptions().unique(true)
            )
    }

    fun convertAndUpload(player: Player, sourceName: String)
    {
        val provider = Versioned.toProvider().getSlimeProvider()

        // Skip the round-trip if the slime is already legacy-readable.
        val currentVersion = runCatching { provider.versionOf(sourceName) }.getOrNull()
        if (currentVersion != null && currentVersion <= 9)
        {
            player.sendMessage(
                "${CC.YELLOW}${sourceName} is already legacy (slime v$currentVersion); no conversion needed."
            )
            return
        }

        player.sendMessage("${CC.YELLOW}Loading ${CC.WHITE}$sourceName${CC.YELLOW} read-only for export...")

        runCatching {
            provider.loadAndRegisterTemplate(sourceName, readOnly = true)
        }.onFailure {
            player.sendMessage("${CC.RED}Could not load $sourceName: ${it.message ?: it.javaClass.simpleName}")
            return
        }

        val source = Bukkit.getWorld(sourceName)
            ?: return run {
                player.sendMessage("${CC.RED}World $sourceName did not register after load.")
            }

        val targetName = "legacyexport_${sourceName}_${UUID.randomUUID().toString().take(8)}"
        val target = WorldCreator(targetName)
            .type(WorldType.FLAT)
            .generator(EmptyChunkGenerator)
            .createWorld()
            ?: return run {
                player.sendMessage("${CC.RED}Failed to create vanilla export world.")
            }

        // ASP keeps chunks lazy; the authoritative chunk list lives in SlimeWorld.chunkStorage.
        val slimeInstance = AdvancedSlimePaperAPI.instance().getLoadedWorld(sourceName)
            ?: return run {
                player.sendMessage("${CC.RED}ASP did not return a SlimeWorldInstance for $sourceName.")
            }

        val chunkCoords = slimeInstance.chunkStorage.map { it.x to it.z }
        player.sendMessage("${CC.YELLOW}Copying ${chunkCoords.size} chunks (this blocks the main thread; please wait)...")

        var copiedTileEntities = 0
        for ((cx, cz) in chunkCoords)
        {
            val srcChunk = source.getChunkAt(cx, cz)
            srcChunk.load(true)
            val tgtChunk = target.getChunkAt(cx, cz)
            tgtChunk.load(true)

            val maxY = source.maxHeight - 1
            for (x in 0..15)
            {
                for (z in 0..15)
                {
                    for (y in 0..maxY)
                    {
                        val src = srcChunk.getBlock(x, y, z)
                        if (src.type == Material.AIR) continue

                        val tgt = tgtChunk.getBlock(x, y, z)
                        tgt.blockData = src.blockData
                    }
                }
            }

            for (te in srcChunk.tileEntities)
            {
                val tgt = target.getBlockAt(te.x, te.y, te.z).state
                when
                {
                    te is Sign && tgt is Sign ->
                    {
                        @Suppress("DEPRECATION")
                        te.lines.forEachIndexed { i, line -> tgt.setLine(i, line) }
                        tgt.update(true, false)
                        copiedTileEntities++
                    }
                    te is Container && tgt is Container ->
                    {
                        tgt.inventory.contents = te.inventory.contents
                        tgt.update(true, false)
                        copiedTileEntities++
                    }
                }
            }
        }

        player.sendMessage("${CC.YELLOW}Saved ${copiedTileEntities} tile entities. Persisting world to disk...")
        target.save()
        Bukkit.unloadWorld(target, true)

        val targetFolder = File(Bukkit.getWorldContainer(), targetName)
        if (!targetFolder.isDirectory)
        {
            player.sendMessage("${CC.RED}Vanilla export folder missing after unload — aborting.")
            return
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

    private fun uploadFolder(name: String, folder: File)
    {
        val payload = ByteArrayOutputStream().use { baos ->
            ZipOutputStream(baos).use { zip ->
                // Both sides must be absolute or `relativize` throws on mixed paths
                // when Bukkit's world container is configured relatively.
                val basePath = folder.absoluteFile.toPath().normalize()
                folder.walkTopDown()
                    .filter { it.isFile }
                    .forEach { file ->
                        val rel = basePath.relativize(file.absoluteFile.toPath().normalize()).toString()
                            .replace(File.separatorChar, '/')
                        zip.putNextEntry(ZipEntry(rel))
                        file.inputStream().use { it.copyTo(zip) }
                        zip.closeEntry()
                    }
            }
            baos.toByteArray()
        }

        bucket.find(Filters.eq("filename", name)).firstOrNull()?.let { bucket.delete(it.objectId) }
        bucket.openUploadStream(name, GridFSUploadOptions()).use { it.write(payload) }
    }

    private object EmptyChunkGenerator : ChunkGenerator()
    {
        override fun canSpawn(world: World, x: Int, z: Int) = true
        override fun getDefaultPopulators(world: World): MutableList<BlockPopulator> = mutableListOf()
        override fun getFixedSpawnLocation(world: World, random: Random) =
            org.bukkit.Location(world, 0.0, 64.0, 0.0)
    }
}
