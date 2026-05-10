package gg.solara.practice.migration

import com.mongodb.client.gridfs.GridFSBuckets
import com.mongodb.client.model.Filters
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Default
import gg.scala.commons.acf.annotation.Single
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.scala.store.ScalaDataStoreShared
import me.lucko.helper.Schedulers
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import java.io.ByteArrayInputStream
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.util.UUID
import java.util.concurrent.ExecutionException
import java.util.logging.Level
import java.util.zip.ZipInputStream

/**
 * Legacy-fleet counterpart to [MapLegacyExporter]: pulls the zipped vanilla world from
 * `UGC.legacy-pending-import`, rewrites chunk NBT for SWM 2.2.1, and runs SWM's
 * `importWorld` to write a v9 slime into the legacy mongo collection.
 */
@AutoRegister
@CommandAlias("maplegacyimport")
@CommandPermission("op")
object MapLegacyImportCommand : ScalaCommand()
{
    private const val BUCKET_NAME = "legacy-pending-import"

    private val swmAvailable: Boolean = runCatching {
        Class.forName("com.grinderwolf.swm.api.SlimePlugin", false, javaClass.classLoader)
    }.isSuccess

    @Default
    fun onImport(player: ScalaPlayer, @Single name: String)
    {
        if (!swmAvailable)
        {
            throw ConditionFailedException("Legacy SWM is not available on this fleet.")
        }

        player.sendMessage("${CC.YELLOW}Looking for ${CC.WHITE}$name${CC.YELLOW} in the import bucket...")

        Schedulers.async().run {
            runCatching { performImport(name) }
                .onFailure { raw ->
                    val ex = unwrap(raw)
                    Schedulers.sync().run {
                        player.sendMessage(
                            "${CC.RED}Import failed: ${ex.javaClass.simpleName}${ex.message?.let { ": $it" } ?: ""}"
                        )
                        player.sendMessage("${CC.GRAY}See the server console for the full stack trace.")
                    }
                }
                .onSuccess {
                    Schedulers.sync().run {
                        player.sendMessage("${CC.GREEN}Imported ${CC.WHITE}$name${CC.GREEN} into legacy SWM mongo as v9.")
                    }
                }
        }
    }

    private fun performImport(name: String)
    {
        val database = ScalaDataStoreShared.INSTANCE
            .getNewMongoConnection()
            .getConnection()
            .getDatabase("UGC")

        val bucket = GridFSBuckets.create(database, BUCKET_NAME)
        val file = bucket.find(Filters.eq("filename", name)).firstOrNull()
            ?: error("No pending export named '$name'. Did you right-click on modern devtools first?")

        val payload = bucket.openDownloadStream(file.objectId).use { it.readAllBytes() }

        val staging = File(Bukkit.getWorldContainer(), "legacyimport_${name}_${UUID.randomUUID().toString().take(8)}")
        staging.mkdirs()

        try
        {
            ZipInputStream(ByteArrayInputStream(payload)).use { zip ->
                generateSequence { zip.nextEntry }.forEach { entry ->
                    if (entry.isDirectory)
                    {
                        File(staging, entry.name).mkdirs()
                    }
                    else
                    {
                        val out = File(staging, entry.name)
                        out.parentFile?.mkdirs()
                        out.outputStream().use { zip.copyTo(it) }
                    }
                    zip.closeEntry()
                }
            }

            Bukkit.getLogger().info(
                "Staging contents for $name: " + staging.walkTopDown()
                    .filter { it.isFile }
                    .map { staging.toPath().relativize(it.toPath()).toString() }
                    .toList()
            )

            // SWM 2.2.1 only parses pre-1.18 chunk NBT; rewrite Paper 1.21's flat layout.
            val regionDir = File(staging, "region")
            if (regionDir.isDirectory)
            {
                runCatching { SwmChunkTranslator.translateRegionFolder(regionDir) }
                    .onFailure {
                        Bukkit.getLogger().log(
                            Level.WARNING,
                            "Chunk NBT translation failed for $name; SWM import will likely fail next.",
                            it
                        )
                    }
            }
            else
            {
                Bukkit.getLogger().warning("No region/ directory in staging for $name — SWM will refuse the world.")
            }

            // SWM API resolved reflectively so this class still loads on modern.
            val swmPlugin = Bukkit.getServer().pluginManager.getPlugin("SlimeWorldManager")
                ?: error("SlimeWorldManager plugin not loaded.")
            val loader = swmPlugin.javaClass.getMethod("getLoader", String::class.java).invoke(swmPlugin, "mongodb")
                ?: error("SWM mongo loader not configured.")

            val loaderClass = Class.forName("com.grinderwolf.swm.api.loaders.SlimeLoader")
            val importMethod = swmPlugin.javaClass
                .getMethod("importWorld", File::class.java, String::class.java, loaderClass)

            // Drop any prior v9 copy — SWM's importWorld refuses to overwrite.
            if (loaderClass.getMethod("worldExists", String::class.java).invoke(loader, name) == true)
            {
                loaderClass.getMethod("deleteWorld", String::class.java).invoke(loader, name)
            }

            // SWM expects the main thread.
            val future = Schedulers.sync().supply {
                runCatching { importMethod.invoke(swmPlugin, staging, name, loader) }
            }
            future.join().exceptionOrNull()?.let { raw ->
                val unwrapped = unwrap(raw)
                Bukkit.getLogger().log(Level.SEVERE, "SWM importWorld failed for $name", unwrapped)
                throw unwrapped
            }

            bucket.delete(file.objectId)
        }
        finally
        {
            staging.deleteRecursively()
        }
    }

    private fun unwrap(throwable: Throwable): Throwable
    {
        var cursor: Throwable = throwable
        while (cursor is InvocationTargetException || cursor is ExecutionException)
        {
            cursor = cursor.cause ?: break
        }
        return cursor
    }
}
