package mc.arch.minigames.persistent.housing.game.menu.house.music

import com.cryptomorin.xseries.XMaterial
import mc.arch.minigames.persistent.housing.api.model.PlayerHouse
import mc.arch.minigames.persistent.housing.game.getReference
import mc.arch.minigames.persistent.housing.game.menu.house.MainHouseMenu
import mc.arch.minigames.persistent.housing.game.music.HousingMusicService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import kotlin.collections.set

class HouseMusicSelectionMenu(val house: PlayerHouse) : PaginatedMenu()
{
    private val discTypes = listOf(
        XMaterial.MUSIC_DISC_STAL,
        XMaterial.MUSIC_DISC_CHIRP,
        XMaterial.MUSIC_DISC_BLOCKS,
        XMaterial.MUSIC_DISC_11,
        XMaterial.MUSIC_DISC_13,
        XMaterial.MUSIC_DISC_CAT,
        XMaterial.MUSIC_DISC_FAR,
        XMaterial.MUSIC_DISC_MALL,
        XMaterial.MUSIC_DISC_MELLOHI
    )

    init
    {
        placeholdBorders = true
        updateAfterClick = true
    }

    override fun size(buttons: Map<Int, Button>) = 45
    override fun getMaxItemsPerPage(player: Player) = 21
    override fun getAllPagesButtonSlots() = (10..16).toList() + (19..25).toList() + (28..34).toList()

    override fun getGlobalButtons(player: Player): Map<Int, Button> = mutableMapOf(
        38 to ItemBuilder.of(XMaterial.BARRIER)
            .name("${CC.RED}Stop Music")
            .addToLore(
                "${CC.GRAY}Stop playing music in",
                "${CC.GRAY}your creative.",
                "",
                if (house.music != null) "${CC.YELLOW}Click to stop the current track!" else "${CC.GRAY}No track is currently selected."
            )
            .toButton { _, _ ->
                house.music = null
                house.save()

                house.getReference()?.onlinePlayers?.mapNotNull {
                    Bukkit.getPlayer(it)
                }?.forEach { other ->
                    HousingMusicService.stopPlayingSong(other)
                }

                player.sendMessage("${CC.YELLOW}Music has been turned off for your creative.")
                Button.playNeutral(player)
            },
        40 to MainHouseMenu.mainMenuButton(house)
    )

    override fun getPrePaginatedTitle(player: Player): String = "Viewing All Music"

    override fun getAllPagesButtons(player: Player): Map<Int, Button> = mutableMapOf<Int, Button>().also { buttons ->
        HousingMusicService.listSongs().forEach { song ->
            val isCurrent = house.music == song.name

            buttons[buttons.size] = ItemBuilder.of(discTypes.random())
                .name("${if (isCurrent) CC.B_GREEN else CC.GREEN}${normalize(song.name)}")
                .addFlags(ItemFlag.HIDE_ATTRIBUTES)
                .addToLore(
                    "",
                    if (isCurrent) "${CC.AQUA}This is your current track!" else "${CC.YELLOW}Click to select this track!",
                )
                .toButton { _, _ ->
                    if (house.music == song.name)
                    {
                        player.sendMessage("${CC.YELLOW}That track is already selected!")
                        Button.playFail(player)
                        return@toButton
                    }

                    house.music = song.name
                    house.save()

                    house.getReference()?.onlinePlayers?.mapNotNull {
                        Bukkit.getPlayer(it)
                    }?.forEach { other ->
                        HousingMusicService.playSong(song.name, other)
                    }

                    player.sendMessage("${CC.B_GREEN}SUCCESS! ${CC.GREEN}You have selected the song ${CC.WHITE}${song.name}")
                    Button.playNeutral(player)
                }
        }
    }

    fun normalize(text: String) = text.split("_").joinToString(" ") { str ->
        str.lowercase().replaceFirstChar { it.uppercaseChar() }
    }
}
