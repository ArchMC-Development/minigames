package gg.tropic.practice.schematics

import com.mongodb.client.MongoDatabase
import com.mongodb.client.gridfs.GridFSBucket
import com.mongodb.client.gridfs.GridFSBuckets
import com.mongodb.client.gridfs.model.GridFSFile
import com.mongodb.client.model.Filters
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.sk89q.jnbt.NBTOutputStream
import com.sk89q.worldedit.LocalSession
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.extent.clipboard.io.SchematicWriter
import gg.scala.store.ScalaDataStoreShared
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.io.*
import java.util.concurrent.CompletableFuture
import java.util.zip.GZIPOutputStream

/**
 * GridFS-based schematic storage system
 * Stores and retrieves schematics from MongoDB GridFS
 */
class GridFSSchematicProvider
{
    private val database: MongoDatabase = ScalaDataStoreShared.INSTANCE
        .getNewMongoConnection()
        .getConnection()
        .getDatabase("worlddb")

    private val bucket: GridFSBucket = GridFSBuckets.create(database, "schematics")

    init
    {
        // Create unique index on filename
        database.getCollection("schematics.files")
            .createIndex(
                Indexes.ascending("filename"),
                IndexOptions().unique(true)
            )
    }

    /**
     * Save a schematic to GridFS
     * @param name The name/identifier for the schematic
     * @param schematicData The raw schematic file bytes
     * @return CompletableFuture<Boolean> indicating success
     */
    fun saveSchematic(name: String, schematicData: ByteArray) = CompletableFuture
        .runAsync {
            deleteSchematicSync(name)
            ByteArrayInputStream(schematicData).use { inputStream ->
                bucket.uploadFromStream(name, inputStream)
            }
        }

    /**
     * Save a schematic file to GridFS
     * @param name The name/identifier for the schematic
     * @param schematicFile The schematic file
     * @return CompletableFuture<Boolean> indicating success
     */
    fun saveSchematic(name: String, schematicFile: File) = CompletableFuture
        .supplyAsync {
            readFileToBytes(schematicFile)
        }
        .thenComposeAsync { saveSchematic(name, it) }

    /**
     * Load a schematic from GridFS
     * @param name The name/identifier of the schematic
     * @return CompletableFuture<Schematic?> or null if not found
     */
    fun loadSchematic(name: String) = CompletableFuture
        .supplyAsync {
            if (!schematicExistsSync(name))
            {
                return@supplyAsync null
            }

            ByteArrayOutputStream().use { outputStream ->
                bucket.downloadToStream(name, outputStream)
                SchematicUtil.loadSchematicFromInputStream(
                    ByteArrayInputStream(outputStream.toByteArray())
                )
            }
        }

    /**
     * Load schematic data as bytes
     * @param name The name/identifier of the schematic
     * @return CompletableFuture<ByteArray?> or null if not found
     */
    fun loadSchematicBytes(name: String) = CompletableFuture
        .supplyAsync {
            if (!schematicExistsSync(name))
            {
                return@supplyAsync null
            }

            ByteArrayOutputStream().use { outputStream ->
                bucket.downloadToStream(name, outputStream)
                outputStream.toByteArray()
            }
        }

    /**
     * Check if a schematic exists
     * @param name The name/identifier of the schematic
     * @return CompletableFuture<Boolean>
     */
    fun schematicExists(name: String) = CompletableFuture.supplyAsync { schematicExistsSync(name) }

    /**
     * Delete a schematic
     * @param name The name/identifier of the schematic
     * @return CompletableFuture<Boolean> indicating success
     */
    fun deleteSchematic(name: String) = CompletableFuture.supplyAsync {
        deleteSchematicSync(name)
    }

    /**
     * List all available schematics
     * @return CompletableFuture<List<String>>
     */
    fun listSchematics() = CompletableFuture
        .supplyAsync {
            bucket.find()
                .map { it.filename }
                .toList()
        }

    /**
     * Save current WorldEdit clipboard as a schematic
     * Requires WorldEdit to be installed
     * @param player The player whose clipboard to save
     * @param name The name for the schematic
     * @return CompletableFuture<Boolean> indicating success
     */
    fun saveFromClipboard(player: Player, name: String) = CompletableFuture.supplyAsync {
        val session = WorldEdit.getInstance().getSession(player.name)
        if (session.clipboard == null)
        {
            return@supplyAsync false
        }

        val byteArrayOutputStream = ByteArrayOutputStream()
        val gzipOutputStream = GZIPOutputStream(byteArrayOutputStream)
        val nbtOutputStream = NBTOutputStream(gzipOutputStream)

        val writer = SchematicWriter(nbtOutputStream)
        writer.write(session.clipboard.clipboard, session.selectionWorld.worldData)
        writer.close()

        gzipOutputStream.close()
        saveSchematic(name, byteArrayOutputStream.toByteArray()).join()
    }


    // Synchronous helper methods for use within locks
    private fun schematicExistsSync(name: String): Boolean
    {
        return try
        {
            val file: GridFSFile? = bucket.find(Filters.eq("filename", name)).first()
            file != null
        } catch (e: Exception)
        {
            false
        }
    }

    private fun deleteSchematicSync(name: String): Boolean
    {
        return try
        {
            val file: GridFSFile? = bucket.find(Filters.eq("filename", name)).first()
            if (file != null)
            {
                bucket.delete(file.objectId)
                true
            } else
            {
                false
            }
        } catch (e: Exception)
        {
            Bukkit.getLogger().warning("Error deleting schematic $name: ${e.message}")
            false
        }
    }

    private fun readFileToBytes(file: File): ByteArray
    {
        FileInputStream(file).use { fis ->
            ByteArrayOutputStream().use { baos ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1)
                {
                    baos.write(buffer, 0, bytesRead)
                }
                return baos.toByteArray()
            }
        }
    }
}
