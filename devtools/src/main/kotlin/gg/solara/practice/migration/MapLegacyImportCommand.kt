package gg.solara.practice.migration

import com.mongodb.client.model.Filters
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Default
import gg.scala.commons.acf.annotation.Single
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import me.lucko.helper.Schedulers
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import java.io.ByteArrayInputStream
import java.io.File
import java.util.UUID
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
                    Bukkit.getLogger().log(Level.SEVERE, "SWM importWorld failed for $name", ex)
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
        val bucket = pendingBucket(LEGACY_PENDING_BUCKET)
        val file = bucket.find(Filters.eq("filename", name)).firstOrNull()
            ?: error("No pending export named '$name'. Did you /maplegacyconvert on modern devtools first?")

        val payload = bucket.openDownloadStream(file.objectId).use { it.readAllBytes() }
        val staging = File(Bukkit.getWorldContainer(), "legacyimport_${name}_${UUID.randomUUID().toString().take(8)}")
        staging.mkdirs()

        try
        {
            unzipInto(payload, staging)

            File(staging, "region")
                .takeIf { it.isDirectory }
                ?.also { dir ->
                    runCatching { SwmChunkTranslator.translateRegionFolder(dir) }
                        .onFailure {
                            Bukkit.getLogger().log(
                                Level.WARNING,
                                "Chunk NBT translation failed for $name; SWM import will likely fail next.",
                                it
                            )
                        }
                }
                ?: Bukkit.getLogger().warning("No region/ directory in staging for $name — SWM will refuse the world.")

            runSwmImport(name, staging)
            bucket.delete(file.objectId)
        }
        finally
        {
            staging.deleteRecursively()
        }
    }

    private fun unzipInto(payload: ByteArray, staging: File)
    {
        ZipInputStream(ByteArrayInputStream(payload)).use { zip ->
            generateSequence { zip.nextEntry }.forEach { entry ->
                val out = File(staging, entry.name)
                if (entry.isDirectory)
                {
                    out.mkdirs()
                }
                else
                {
                    out.parentFile?.mkdirs()
                    out.outputStream().use { zip.copyTo(it) }
                }
                zip.closeEntry()
            }
        }
    }

    /** SWM API resolved reflectively so this class still loads on modern. */
    private fun runSwmImport(name: String, staging: File)
    {
        val swmPlugin = Bukkit.getServer().pluginManager.getPlugin("SlimeWorldManager")
            ?: error("SlimeWorldManager plugin not loaded.")
        val loader = swmPlugin.javaClass.getMethod("getLoader", String::class.java).invoke(swmPlugin, "mongodb")
            ?: error("SWM mongo loader not configured.")

        val loaderClass = Class.forName("com.grinderwolf.swm.api.loaders.SlimeLoader")
        val importMethod = swmPlugin.javaClass
            .getMethod("importWorld", File::class.java, String::class.java, loaderClass)

        // SWM's importWorld refuses to overwrite — drop any prior copy first.
        if (loaderClass.getMethod("worldExists", String::class.java).invoke(loader, name) == true)
        {
            loaderClass.getMethod("deleteWorld", String::class.java).invoke(loader, name)
        }

        // SWM expects the main thread.
        Schedulers.sync().supply {
            runCatching { importMethod.invoke(swmPlugin, staging, name, loader) }
        }.join().exceptionOrNull()?.let { throw unwrap(it) }
    }
}
