package gg.solara.practice.editor

import com.cryptomorin.xseries.XMaterial
import com.sk89q.worldedit.Vector
import gg.solara.practice.editor.EditorGenerator.VoidWorldGenerator
import gg.solara.practice.utilities.WorldEditUtils
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.entity.Player
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.zip.ZipInputStream

/**
 * @author Subham
 * @since 6/24/25
 */
class WorldEditable(private val file: File) : Editable
{
    override val icon: XMaterial
        get() = XMaterial.GRASS_BLOCK
    override val displayName: String
        get() = file.name.split(".").first()

    override fun prepareWorld(player: Player): World
    {
        // Generate unique world directory name
        val worldName = "editor_temp_${UUID.randomUUID()}"
        val worldContainer = Bukkit.getWorldContainer()
        val worldDir = File(worldContainer, worldName)

        // Create the world directory
        worldDir.mkdirs()

        player.sendMessage("${CC.GREEN}Unzipping file...")
        // Unzip the file to the world directory
        unzipFile(file, worldDir)

        // Create and load the world
        val worldCreator = WorldCreator.name(worldName).generator(VoidWorldGenerator).generateStructures(false)
        val newWorld = worldCreator
            .createWorld()
            .apply {
                setGameRuleValue("doMobSpawning", "false")
            }
            ?: throw RuntimeException("Failed to create world: $worldName")

        return newWorld
    }

    private fun unzipFile(zipFile: File, destDir: File)
    {
        zipFile.inputStream().use { fileInputStream ->
            ZipInputStream(fileInputStream).use { zipInputStream ->
                var entry = zipInputStream.nextEntry

                while (entry != null)
                {
                    // Skip the first directory level - extract contents one level up
                    val entryPath = entry.name
                    val pathParts = entryPath.split("/")

                    // Skip if this is just the root directory entry
                    if (pathParts.size <= 1)
                    {
                        zipInputStream.closeEntry()
                        entry = zipInputStream.nextEntry
                        continue
                    }

                    // Remove the first directory from the path
                    val adjustedPath = pathParts.drop(1).joinToString("/")
                    val entryFile = File(destDir, adjustedPath)

                    if (entry.isDirectory)
                    {
                        // Create directory
                        entryFile.mkdirs()
                    } else
                    {
                        // Create parent directories if they don't exist
                        entryFile.parentFile?.mkdirs()

                        // Extract file
                        FileOutputStream(entryFile).use { outputStream ->
                            zipInputStream.copyTo(outputStream)
                        }
                    }

                    zipInputStream.closeEntry()
                    entry = zipInputStream.nextEntry
                }
            }
        }
    }
}
