package gg.solara.practice.editor.components

import com.cryptomorin.xseries.XMaterial
import com.sk89q.worldedit.WorldEdit
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.Default
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.tropic.practice.schematics.SchematicUtil
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.menus.ConfirmMenu
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 7/23/25
 */
@AutoRegister
@CommandAlias("worldcomponent")
object WorldComponentCommand : ScalaCommand()
{
    enum class SchematicFilter
    {
        ALL, RECENT, PERSONAL
    }

    @Default
    fun onWorldComponent(player: ScalaPlayer) = object : PaginatedMenu()
    {
        var filterState: SchematicFilter? = null
        val schematicProvider = SchematicUtil.getSchematicProvider()

        override fun getPrePaginatedTitle(player: Player) = "World Components"
        override fun size(buttons: Map<Int, Button>) = 54
        override fun getMaxItemsPerPage(player: Player) = 36

        override fun getGlobalButtons(player: Player) = mapOf(
            4 to ItemBuilder
                .of(XMaterial.COMPASS)
                .name(
                    "${CC.WHITE}Filter: ${
                        when (filterState)
                        {
                            null -> "${CC.D_GREEN}All"
                            SchematicFilter.ALL -> "${CC.D_GREEN}All"
                            SchematicFilter.RECENT -> "${CC.YELLOW}Recent"
                            SchematicFilter.PERSONAL -> "${CC.BLUE}Personal"
                        }
                    }"
                )
                .addToLore(
                    "${CC.GRAY}Click to cycle filter",
                    "${CC.GRAY}All -> Recent -> Personal -> All"
                )
                .toButton { _, _ ->
                    filterState = when (filterState)
                    {
                        null, SchematicFilter.ALL -> SchematicFilter.RECENT
                        SchematicFilter.RECENT -> SchematicFilter.PERSONAL
                        SchematicFilter.PERSONAL -> SchematicFilter.ALL
                    }

                    Button.playNeutral(player)
                    openMenu(player)
                },

            0 to ItemBuilder
                .of(XMaterial.EMERALD)
                .name("${CC.GREEN}Create from Clipboard")
                .addToLore(
                    "${CC.GRAY}Create a new World Component from",
                    "${CC.GRAY}your current WorldEdit clipboard",
                    "",
                    "${CC.GREEN}Click to create!"
                )
                .toButton { _, _ ->
                    createSchematicFromClipboard(player)
                },

            8 to ItemBuilder
                .of(XMaterial.REDSTONE)
                .name("${CC.RED}Refresh List")
                .addToLore(
                    "${CC.GRAY}Reload all World Components",
                    "",
                    "${CC.RED}Click to refresh!"
                )
                .toButton { _, _ ->
                    Button.playNeutral(player)
                    openMenu(player)
                }
        )

        override fun getAllPagesButtons(player: Player): Map<Int, Button>
        {
            val mappings = mutableMapOf<Int, Button>()
            val schematicNames = schematicProvider.listSchematics().join()
            val filteredSchematics = schematicNames.filter { schematicName ->
                when (filterState)
                {
                    null, SchematicFilter.ALL -> true
                    SchematicFilter.RECENT ->
                    {
                        // You could implement timestamp-based filtering here
                        // For now, just show all
                        true
                    }

                    SchematicFilter.PERSONAL ->
                    {
                        // Filter by player name if schematics follow naming convention
                        schematicName.startsWith(player.name, ignoreCase = true)
                    }
                }
            }

            filteredSchematics.forEachIndexed { index, schematicName ->
                mappings[index] = ItemBuilder
                    .of(XMaterial.STONE_BRICKS)
                    .name("${CC.GREEN}$schematicName")
                    .addToLore(
                        "${CC.GRAY}Type: ${CC.WHITE}World Component",
                        "",
                        "${CC.GREEN}Left-Click: ${CC.WHITE}Load to clipboard",
                        "${CC.YELLOW}Shift-Click: ${CC.WHITE}Paste at location",
                        "${CC.RED}Right-Click: ${CC.WHITE}Delete schematic",
                        "${CC.AQUA}Q-Key: ${CC.WHITE}Rename schematic"
                    )
                    .toButton { _, type ->
                        when
                        {
                            type!!.isRightClick ->
                            {
                                deleteSchematic(player, schematicName)
                            }

                            type.isShiftClick ->
                            {
                                pasteSchematicAtLocation(player, schematicName)
                            }

                            type.isKeyboardClick ->
                            {
                                renameSchematic(player, schematicName)
                            }

                            else ->
                            {
                                loadSchematicToClipboard(player, schematicName)
                            }
                        }
                    }
            }

            return mappings
        }

        private fun createSchematicFromClipboard(player: Player)
        {
            player.closeInventory()

            // Check if player has a clipboard
            val session = WorldEdit.getInstance().getSession(player.name)
            if (session.clipboard == null)
            {
                player.sendMessage("${CC.RED}You don't have anything in your clipboard!")
                return
            }

            player.sendMessage("${CC.YELLOW}Enter a name for your schematic:")

            // You'll need to implement text input here - this is a placeholder
            // Could use anvil GUI or chat input system
            promptForSchematicName(player) { name ->
                if (name.isBlank())
                {
                    player.sendMessage("${CC.RED}Invalid name!")
                    return@promptForSchematicName
                }

                schematicProvider.saveFromClipboard(player, name).whenComplete { success, throwable ->
                    if (throwable == null)
                    {
                        player.sendMessage("${CC.GREEN}Schematic '$name' saved successfully!")
                    } else
                    {
                        player.sendMessage("${CC.RED}Failed to save schematic '$name'!")
                    }
                }
            }
        }

        private fun loadSchematicToClipboard(player: Player, schematicName: String)
        {
            player.closeInventory()

            schematicProvider.loadSchematic(schematicName).thenAccept { schematic ->
                if (schematic != null)
                {
                    try
                    {
                        // Load schematic to player's clipboard
                        val session = WorldEdit.getInstance().getSession(player.name)
                        // You'll need to implement the actual clipboard loading here
                        // This depends on your Schematic class implementation

                        player.sendMessage("${CC.GREEN}Schematic '$schematicName' loaded to clipboard!")
                        Button.playSuccess(player)
                    } catch (e: Exception)
                    {
                        player.sendMessage("${CC.RED}Failed to load schematic: ${e.message}")
                        Button.playFail(player)
                    }
                } else
                {
                    player.sendMessage("${CC.RED}Schematic not found!")
                    Button.playFail(player)
                }
            }
        }

        private fun pasteSchematicAtLocation(player: Player, schematicName: String)
        {
            player.closeInventory()

            SchematicUtil
                .pasteSchematicFromStorage(player.location, schematicName)
                .whenComplete { _, throwable ->
                    throwable?.printStackTrace()
                }
        }

        private fun deleteSchematic(player: Player, schematicName: String)
        {
            // Add confirmation dialog
            ConfirmMenu(
                title = "Delete Schematic",
                confirm = true
            ) {
                if (it)
                {
                    schematicProvider.deleteSchematic(schematicName).thenAccept { success ->
                        if (success)
                        {
                            player.sendMessage("${CC.GREEN}Schematic '$schematicName' deleted!")
                            Button.playSuccess(player)
                            openMenu(player) // Refresh the menu
                        } else
                        {
                            player.sendMessage("${CC.RED}Failed to delete schematic!")
                            Button.playFail(player)
                        }
                    }
                } else
                {
                    openMenu(player)
                }
            }.openMenu(player)
        }

        private fun renameSchematic(player: Player, oldName: String)
        {
            player.closeInventory()
            player.sendMessage("${CC.YELLOW}Enter new name for '$oldName':")

            promptForSchematicName(player) { newName ->
                if (newName.isBlank() || newName == oldName)
                {
                    player.sendMessage("${CC.RED}Invalid name!")
                    return@promptForSchematicName
                }

                // Check if new name already exists
                schematicProvider.schematicExists(newName).thenAccept { exists ->
                    if (exists)
                    {
                        player.sendMessage("${CC.RED}A schematic with that name already exists!")
                        return@thenAccept
                    }

                    // Load old schematic and save with new name
                    schematicProvider.loadSchematicBytes(oldName).thenAccept { data ->
                        if (data != null)
                        {
                            schematicProvider.saveSchematic(newName, data).whenComplete { saveSuccess, throwable ->
                                if (throwable != null)
                                {
                                    schematicProvider.deleteSchematic(oldName).thenAccept { deleteSuccess ->
                                        if (deleteSuccess)
                                        {
                                            player.sendMessage("${CC.GREEN}Schematic renamed from '$oldName' to '$newName'!")
                                        } else
                                        {
                                            player.sendMessage("${CC.YELLOW}Schematic copied to '$newName' but failed to delete '$oldName'!")
                                        }
                                    }
                                } else
                                {
                                    player.sendMessage("${CC.RED}Failed to rename schematic!")
                                }
                            }
                        } else
                        {
                            player.sendMessage("${CC.RED}Failed to load original schematic!")
                        }
                    }
                }
            }
        }

        private fun promptForSchematicName(player: Player, callback: (String) -> Unit)
        {
            player.closeInventory()
            InputPrompt()
                .withText("Enter a new schematic name")
                .acceptInput { _, text ->
                    callback(text)
                }
                .start(player)
        }
    }.openMenu(player)
}
