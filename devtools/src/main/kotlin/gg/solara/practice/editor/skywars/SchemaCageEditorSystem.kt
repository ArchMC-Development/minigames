package gg.solara.practice.editor.skywars

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.hotbar.HotbarPreset
import gg.scala.lemon.hotbar.HotbarPresetHandler
import gg.scala.lemon.hotbar.entry.impl.DynamicHotbarPresetEntry
import gg.tropic.game.extensions.cosmetics.cages.SchemaCage
import gg.tropic.game.extensions.cosmetics.cages.SchemaCageDataSync
import gg.solara.practice.editor.EditorGenerator
import me.lucko.helper.Events
import me.lucko.helper.Schedulers
import me.lucko.helper.terminable.composite.CompositeTerminable
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.*

/**
 * @author GrowlyX
 * @since 7/18/2024
 */
class SchemaCageEditorSystem(private val player: Player, private val instance: SchemaCageEditorInstance)
{
    companion object
    {
        val instances = mutableMapOf<UUID, SchemaCageEditorSystem>()
    }

    private val editorID = "editor-${player.uniqueId}"
    private val baseBlock = Vector(0, 64, 0)

    private val terminable = CompositeTerminable.create()
    private var initialized = false

    fun initialize()
    {
        Events
            .subscribe(PlayerQuitEvent::class.java)
            .handler {
                if (it.player.uniqueId == player.uniqueId)
                {
                    terminable.closeAndReportException()
                }
            }
            .bindWith(terminable)

        fun editAndSaveSchema(block: SchemaCage.() -> Unit)
        {
            val cage = instance.schemaCage
            block(cage)

            val cached = SchemaCageDataSync.cached()
            cached.schemas[cage.id] = cage
            SchemaCageDataSync.sync(cached)
        }

        Events
            .subscribe(BlockBreakEvent::class.java)
            .filter { it.player.uniqueId == player.uniqueId }
            .handler {
                val baseBlockLocation = it.block.world.getBlockAt(baseBlock.toLocation(it.block.world))
                if (it.block.location.toVector().equals(baseBlock))
                {
                    val itemInHand = if (it.player.itemInHand.type.isBlock)
                    {
                        it.player.itemInHand.clone()
                    } else
                    {
                        ItemStack(Material.GLASS)
                    }

                    it.player.sendMessage("${CC.GREEN}Replaced base block!")
                    Schedulers.sync()
                        .runLater({
                            val state = it.block.state
                            state.type = itemInHand.type
                            state.rawData = itemInHand.data.data
                            state.update(true, true)

                            editAndSaveSchema {
                                addBlockModifier(baseBlock = baseBlockLocation, it.block)
                            }
                        }, 1L)
                        .bindWith(terminable)
                    return@handler
                }

                editAndSaveSchema {
                    removeBlockModifier(baseBlock = baseBlockLocation, it.block)
                }

                it.player.sendMessage("${CC.RED}Removed block!")
            }
            .bindWith(terminable)

        Events
            .subscribe(BlockPlaceEvent::class.java)
            .filter { it.player.uniqueId == player.uniqueId }
            .handler {
                val baseBlockLocation = it.block.world.getBlockAt(baseBlock.toLocation(it.block.world))
                editAndSaveSchema {
                    addBlockModifier(baseBlock = baseBlockLocation, it.block)
                }

                it.player.sendMessage("${CC.GREEN}Added block!")
            }
            .bindWith(terminable)

        instances[player.uniqueId] = this

        terminable.with {
            player.inventory.clear()
            player.updateInventory()

            player.teleport(
                Location(
                    Bukkit.getWorld("world"),
                    0.0, 100.0, 0.0
                )
            )

            HotbarPresetHandler.forget(editorID)
            instances.remove(player.uniqueId)
        }

        populateInventoryHotbar()
        visit()
    }

    fun visit()
    {
        if (initialized)
        {
            return
        }
        initialized = true

        val newWorld = EditorGenerator.createNewEmptyWorld()
        val worldName = newWorld.name
        terminable.with {
            if (Bukkit.getWorld(worldName) == null)
            {
                return@with
            }

            Bukkit.unloadWorld(worldName, false)
        }

        player.gameMode = GameMode.CREATIVE
        player.allowFlight = true
        player.isFlying = true
        player.flySpeed = 0.5f
        player.teleport(
            Location(
                newWorld, -3.0, 65.0, -3.0
            )
        )

        player.sendMessage("${CC.GREEN}Editing...")
        player.playSound(player.location, Sound.NOTE_PLING, 1.0f, 1.0f)

        val worldBaseBlock = newWorld.getBlockAt(baseBlock.toLocation(newWorld))
        instance.schemaCage.applyTo(worldBaseBlock)
    }

    private fun populateInventoryHotbar()
    {
        val hotbarPreset = HotbarPreset()
        hotbarPreset.addSlot(8, DynamicHotbarPresetEntry().apply {
            onBuild = {
                ItemBuilder.of(XMaterial.RED_DYE)
                    .glow()
                    .name("${CC.RED}Quit ${CC.GRAY}(Right Click)")
                    .build()
            }

            onClick = context@{
                player.playSound(player.location, Sound.NOTE_STICKS, 1.0f, 1.0f)
                player.sendMessage("${CC.RED}Leaving...")
                instances.remove(player.uniqueId)?.terminable?.closeAndReportException()
            }
        })


        hotbarPreset.applyToPlayer(player)
        HotbarPresetHandler.startTrackingHotbar(
            editorID,
            hotbarPreset
        )
    }

}
