package gg.tropic.practice.minigame

import gg.tropic.practice.queue.QueueIDParser
import gg.tropic.practice.metadata.SystemMetadataService
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.math.Numbers
import net.evilblock.cubed.util.text.TextSplitter
import org.bukkit.inventory.ItemStack

/**
 * @author Subham
 * @since 6/28/25
 */
data class MiniGameModeMetadata(
    val id: String,
    val description: String,
    val queueId: String,
    val displayName: String,
    val displayItem: ItemStack,
    val mapGroup: String,
    val kitID: String,
    val mode: MiniGameMode,
    val npcSkinValue: String,
    val npcSkinSignature: String,
    val allowMapSelection: Boolean = true,
    val allowRejoins: Boolean = true,
)
{
    fun playersPlaying() = SystemMetadataService.getPlaying(queueId)

    fun toNPCHeader() = listOf(
        "${CC.B_YELLOW}CLICK TO PLAY",
        "${CC.RED}$displayName",
        "${CC.B_YELLOW}${
            Numbers.format(playersPlaying())
        } player${
            if (playersPlaying() == 1) "" else "s"
        }"
    )

    fun toQueueId() = QueueIDParser.parseDetailed(queueId)
    fun toItem() = ItemBuilder.copyOf(displayItem)
        .name("${CC.GREEN}${displayName}")
        .setLore(
            TextSplitter.split(
                text = description,
                linePrefix = CC.GRAY,
                wordSuffix = " "
            )
        )
        .addToLore(
            "",
            "${CC.GREEN}Playing: ${CC.WHITE}${
                Numbers.format(playersPlaying())
            }"
        )

    fun toRawItem() = ItemBuilder.copyOf(displayItem)

    fun toConciseJoinItem() = toItem()
        .addToLore(
            "",
            "${CC.GREEN}Click to join a $displayName game!",
            "${CC.YELLOW}Right-click to select a map!"
        )
}
