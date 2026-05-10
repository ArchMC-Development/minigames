package gg.solara.practice.migration

import com.mongodb.client.gridfs.GridFSBuckets
import com.mongodb.client.model.Filters
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
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
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.ExecutionException
import java.util.logging.Level

private const val MODERN_PENDING_BUCKET = "modern-pending-import"

private val classLoader = MapLegacyConvertCommand::class.java.classLoader

private val swmAvailable: Boolean = runCatching {
    Class.forName("com.grinderwolf.swm.api.SlimePlugin", false, classLoader)
}.isSuccess

private val aspAvailable: Boolean = runCatching {
    Class.forName("com.infernalsuite.asp.api.AdvancedSlimePaperAPI", false, classLoader)
}.isSuccess

/**
 * Modern-fleet command. Delegates to [MapLegacyExporter]; finish on legacy with
 * `/maplegacyimport <name>`.
 */
@AutoRegister
@CommandAlias("maplegacyconvert")
@CommandPermission("op")
object MapLegacyConvertCommand : ScalaCommand()
{
    @Default
    fun onConvert(player: ScalaPlayer, @Single name: String)
    {
        if (!aspAvailable)
        {
            throw ConditionFailedException("ASP is not available on this fleet — run /maplegacyconvert from modern devtools.")
        }
        MapLegacyExporter.convertAndUpload(player.bukkit(), name)
    }
}

/**
 * Legacy-fleet command. Pushes raw v9 bytes into `UGC.modern-pending-import`; finish on
 * modern with `/mapmodernimport <name>`.
 */
@AutoRegister
@CommandAlias("mapmodernconvert")
@CommandPermission("op")
object MapModernConvertCommand : ScalaCommand()
{
    @Default
    fun onConvert(player: ScalaPlayer, @Single name: String)
    {
        if (!swmAvailable)
        {
            throw ConditionFailedException("SWM is not available on this fleet — run /mapmodernconvert from legacy devtools.")
        }

        player.sendMessage("${CC.YELLOW}Reading ${CC.WHITE}$name${CC.YELLOW} from legacy SWM mongo...")

        Schedulers.async().run {
            runCatching { uploadLegacyBytes(name) }
                .onFailure { raw ->
                    val ex = unwrap(raw)
                    Bukkit.getLogger().log(Level.SEVERE, "mapmodernconvert export failed for $name", ex)
                    Schedulers.sync().run {
                        player.sendMessage(
                            "${CC.RED}Conversion failed: ${ex.javaClass.simpleName}${ex.message?.let { ": $it" } ?: ""}"
                        )
                    }
                }
                .onSuccess { size ->
                    Schedulers.sync().run {
                        player.sendMessage("${CC.GREEN}Uploaded ${CC.WHITE}$name${CC.GREEN} ($size bytes) to the modern-pending bucket.")
                        player.sendMessage("${CC.GRAY}Run ${CC.YELLOW}/mapmodernimport $name${CC.GRAY} on the modern fleet to finish.")
                    }
                }
        }
    }

    private fun uploadLegacyBytes(name: String): Int
    {
        // SWM API resolved reflectively so this class still loads on modern.
        val swmPlugin = Bukkit.getServer().pluginManager.getPlugin("SlimeWorldManager")
            ?: error("SlimeWorldManager plugin not loaded.")
        val loader = swmPlugin.javaClass.getMethod("getLoader", String::class.java).invoke(swmPlugin, "mongodb")
            ?: error("SWM mongo loader not configured.")

        val loaderClass = Class.forName("com.grinderwolf.swm.api.loaders.SlimeLoader")
        val exists = loaderClass.getMethod("worldExists", String::class.java).invoke(loader, name) as Boolean
        if (!exists) error("No legacy slime named '$name' in the SWM mongo collection.")

        // readOnly=true to avoid acquiring SWM's write lock.
        val bytes = loaderClass
            .getMethod("loadWorld", String::class.java, Boolean::class.javaPrimitiveType)
            .invoke(loader, name, true) as ByteArray

        val bucket = pendingBucket()
        bucket.find(Filters.eq("filename", name)).firstOrNull()?.let { bucket.delete(it.objectId) }
        bucket.openUploadStream(name).use { it.write(bytes) }
        return bytes.size
    }
}

/**
 * Modern-fleet command. Pulls v9 bytes from `UGC.modern-pending-import`, has ASP read
 * them through a one-shot loader, and re-serialises into the ASP mongo collection as v13.
 */
@AutoRegister
@CommandAlias("mapmodernimport")
@CommandPermission("op")
object MapModernImportCommand : ScalaCommand()
{
    @Default
    fun onImport(player: ScalaPlayer, @Single name: String)
    {
        if (!aspAvailable)
        {
            throw ConditionFailedException("ASP is not available on this fleet — run /mapmodernimport from modern devtools.")
        }

        player.sendMessage("${CC.YELLOW}Looking for ${CC.WHITE}$name${CC.YELLOW} in the modern-pending bucket...")

        Schedulers.async().run {
            runCatching { performModernImport(name) }
                .onFailure { raw ->
                    val ex = unwrap(raw)
                    Bukkit.getLogger().log(Level.SEVERE, "mapmodernimport failed for $name", ex)
                    Schedulers.sync().run {
                        player.sendMessage(
                            "${CC.RED}Import failed: ${ex.javaClass.simpleName}${ex.message?.let { ": $it" } ?: ""}"
                        )
                        player.sendMessage("${CC.GRAY}See the server console for the full stack trace.")
                    }
                }
                .onSuccess {
                    Schedulers.sync().run {
                        player.sendMessage("${CC.GREEN}Imported ${CC.WHITE}$name${CC.GREEN} into the modern ASP collection as v13.")
                    }
                }
        }
    }
}

private fun performModernImport(name: String)
{
    val bucket = pendingBucket()
    val file = bucket.find(Filters.eq("filename", name)).firstOrNull()
        ?: error("No pending modern-import named '$name'. Did you run /mapmodernconvert on legacy first?")

    val bytes = bucket.openDownloadStream(file.objectId).use { it.readAllBytes() }

    // ASP requires the main thread.
    val future = Schedulers.sync().supply {
        runCatching { ModernImportSupport.importBytes(name, bytes) }
    }
    future.join().exceptionOrNull()?.let { throw it }

    bucket.delete(file.objectId)
}

private fun pendingBucket() =
    ScalaDataStoreShared.INSTANCE.getNewMongoConnection().getConnection().getDatabase("UGC").let { db ->
        db.getCollection("$MODERN_PENDING_BUCKET.files")
            .createIndex(Indexes.ascending("filename"), IndexOptions().unique(true))
        GridFSBuckets.create(db, MODERN_PENDING_BUCKET)
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
