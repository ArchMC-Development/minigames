package gg.tropic.practice.portal.procedure

import com.cryptomorin.xseries.XMaterial
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.tropic.practice.portal.LobbyPortal
import gg.tropic.practice.portal.LobbyPortalService
import me.lucko.helper.Events
import me.lucko.helper.Helper
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.cuboid.Cuboid
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import java.util.*

@Service
object PortalCreationProcedure
{
    private val positionMap: MutableMap<UUID, Pair<Location?, Location?>> = mutableMapOf()

    private val wandItem: ItemStack = ItemBuilder.of(XMaterial.GOLDEN_HOE)
        .name("${CC.B_GREEN}Portal Creation Wand")
        .addToLore("${CC.GRAY}Use this wand to create a portal...")
        .build()

    private val procedureMessages: List<String> = listOf(
        "${CC.GRAY}${Constants.ARROW_RIGHT} ${CC.WHITE}Left-Click to set position #1",
        "${CC.GRAY}${Constants.ARROW_RIGHT} ${CC.WHITE}Right-Click to set position #2",
        "${CC.GRAY}${Constants.ARROW_RIGHT} ${CC.WHITE}Shift-Left-Click to finish procedure",
        "${CC.GRAY}${Constants.ARROW_RIGHT} ${CC.WHITE}Shift-Right-Click to abandon procedure"
    )

    @Configure
    fun configure()
    {
        Events.subscribe(PlayerInteractEvent::class.java)
            .handler { event ->
                val player = event.player
                val action = event.action
                val block = event.clickedBlock

                if (!player.hasMetadata("creating-portal")) return@handler
                if (!event.hasItem() || !event.item.isSimilar(wandItem)) return@handler

                if (action.name.startsWith("LEFT_CLICK"))
                {
                    if (action == Action.LEFT_CLICK_BLOCK)
                    {
                        val currentPosition = positionMap[player.uniqueId]
                            ?: Pair(null, null)
                        val newPosition = currentPosition.copy(
                            first = block.location
                        )

                        positionMap[player.uniqueId] = newPosition
                        player.sendMessage("${CC.GREEN}You have set this block to position ${CC.YELLOW}#1${CC.GREEN}.")
                    } else if (action == Action.LEFT_CLICK_AIR)
                    {
                        if (player.isSneaking)
                        {
                            val currentPosition = positionMap[player.uniqueId]

                            if (currentPosition?.first == null || currentPosition.second == null)
                            {
                                player.sendMessage("${CC.RED}You have not completed this procedure! Make sure you set both points of the portal.")
                            } else
                            {
                                val blocksInBetween =
                                    Cuboid(currentPosition.first!!, currentPosition.second!!).getBlockLocations()
                                val portalToCreate = LobbyPortal(
                                    blocks = blocksInBetween.toMutableList()
                                )

                                with(LobbyPortalService.cached())
                                {
                                    this.portals[portalToCreate.identifier] = portalToCreate
                                    LobbyPortalService.sync(this)
                                }

                                player.removeMetadata("creating-portal", Helper.hostPlugin())
                                positionMap.remove(player.uniqueId)
                                player.sendMessage("${CC.GREEN}You have just created a new portal containing ${CC.WHITE}${portalToCreate.blocks.size} blocks${CC.GREEN}.")

                                player.gameMode = GameMode.ADVENTURE
                                player.itemInHand = null
                            }
                        }
                    }
                } else if (action.name.startsWith("RIGHT_CLICK"))
                {
                    if (action == Action.RIGHT_CLICK_BLOCK)
                    {
                        val currentPosition = positionMap[player.uniqueId]
                            ?: Pair(null, null)
                        val newPosition = currentPosition.copy(
                            second = block.location
                        )

                        positionMap[player.uniqueId] = newPosition
                        player.sendMessage("${CC.GREEN}You have set this block to position ${CC.YELLOW}#2${CC.GREEN}.")
                    } else if (action == Action.RIGHT_CLICK_AIR)
                    {
                        if (player.isSneaking)
                        {
                            player.removeMetadata("creating-portal", Helper.hostPlugin())
                            player.sendMessage("${CC.RED}You have abandoned the portal creation procedure.")
                            positionMap.remove(player.uniqueId)

                            player.gameMode = GameMode.ADVENTURE
                            player.itemInHand = null
                        }
                    }
                }
            }
    }

    fun start(player: Player)
    {
        player.inventory.addItem(wandItem)
        player.setMetadata("creating-portal", FixedMetadataValue(Helper.hostPlugin(), true))
        player.sendMessage("${CC.GREEN}You are now starting the portal creation process!")
        procedureMessages.forEach { player.sendMessage(it) }
        player.gameMode = GameMode.SURVIVAL
    }
}
