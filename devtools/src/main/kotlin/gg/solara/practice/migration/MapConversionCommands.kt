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
import java.util.logging.Level

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

        runAsync(
            player = player,
            label = "mapmodernconvert export",
            work = { uploadLegacyBytes(name) },
            onSuccess = { size ->
                player.sendMessage("${CC.GREEN}Uploaded ${CC.WHITE}$name${CC.GREEN} ($size bytes) to the modern-pending bucket.")
                player.sendMessage("${CC.GRAY}Run ${CC.YELLOW}/mapmodernimport $name${CC.GRAY} on the modern fleet to finish.")
            }
        )
    }

    /** SWM API resolved reflectively so this class still loads on modern. */
    private fun uploadLegacyBytes(name: String): Int
    {
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

        val bucket = pendingBucket(MODERN_PENDING_BUCKET)
        bucket.find(Filters.eq("filename", name)).firstOrNull()?.let { bucket.delete(it.objectId) }
        bucket.openUploadStream(name).use { it.write(bytes) }
        return bytes.size
    }
}

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

        runAsync(
            player = player,
            label = "mapmodernimport",
            work = { performModernImport(name) },
            onSuccess = {
                player.sendMessage("${CC.GREEN}Imported ${CC.WHITE}$name${CC.GREEN} into the modern ASP collection as v13.")
            }
        )
    }

    private fun performModernImport(name: String)
    {
        val bucket = pendingBucket(MODERN_PENDING_BUCKET)
        val file = bucket.find(Filters.eq("filename", name)).firstOrNull()
            ?: error("No pending modern-import named '$name'. Did you /mapmodernconvert on legacy first?")

        val bytes = bucket.openDownloadStream(file.objectId).use { it.readAllBytes() }

        // ASP requires the main thread.
        Schedulers.sync().supply {
            runCatching { ModernImportSupport.importBytes(name, bytes) }
        }.join().exceptionOrNull()?.let { throw it }

        bucket.delete(file.objectId)
    }
}

private fun <T> runAsync(player: ScalaPlayer, label: String, work: () -> T, onSuccess: (T) -> Unit)
{
    Schedulers.async().run {
        runCatching(work)
            .onFailure { raw ->
                val ex = unwrap(raw)
                Bukkit.getLogger().log(Level.SEVERE, "$label failed", ex)
                Schedulers.sync().run {
                    player.sendMessage(
                        "${CC.RED}Failed: ${ex.javaClass.simpleName}${ex.message?.let { ": $it" } ?: ""}"
                    )
                    player.sendMessage("${CC.GRAY}See the server console for the full stack trace.")
                }
            }
            .onSuccess { result ->
                Schedulers.sync().run { onSuccess(result) }
            }
    }
}
