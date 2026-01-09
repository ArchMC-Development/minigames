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
        XMaterial.MUSIC_DISC_5,
        XMaterial.MUSIC_DISC_11,
        XMaterial.MUSIC_DISC_13,
        XMaterial.MUSIC_DISC_CAT,
        XMaterial.MUSIC_DISC_CREATOR,
        XMaterial.MUSIC_DISC_FAR,
        XMaterial.MUSIC_DISC_MALL,
        XMaterial.MUSIC_DISC_MELLOHI
    )

    init
    {
        placeholdBorders = true
    }

    override fun size(buttons: Map<Int, Button>) = 45
    override fun getMaxItemsPerPage(player: Player) = 21
    override fun getAllPagesButtonSlots() = (10..16).toList() + (19..25).toList() + (28..34).toList()

    override fun getGlobalButtons(player: Player): Map<Int, Button> = mutableMapOf(
        31 to MainHouseMenu.mainMenuButton(house)
    )

    override fun getPrePaginatedTitle(player: Player): String = "Viewing All Music"

    override fun getAllPagesButtons(player: Player): Map<Int, Button> = mutableMapOf<Int, Button>().also { buttons ->
        HousingMusicService.listSongs().forEach { song ->
            buttons[buttons.size] = ItemBuilder.of(discTypes.random())
                .name("${CC.GREEN}${normalize(song.name)}")
                .addFlags(ItemFlag.HIDE_ATTRIBUTES)
                .addToLore(
                    "",
                    if (house.music != song.name) "${CC.YELLOW}Click to select this track!" else "${CC.AQUA}This is your current track!",
                )
                .toButton { _, _ ->
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

    // this won't catch everything, but most
    fun normalize(text: String) = text.split("_").joinToString(" ") { str ->
        str.lowercase().replaceFirstChar { it.uppercaseChar() }
    }
}