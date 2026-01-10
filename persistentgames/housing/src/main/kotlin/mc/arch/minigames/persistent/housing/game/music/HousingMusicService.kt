package mc.arch.minigames.persistent.housing.game.music

import com.xxmicloxx.NoteBlockAPI.NoteBlockAPI
import com.xxmicloxx.NoteBlockAPI.model.Song
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import mc.arch.minigames.persistent.housing.game.PersistentGameHousing
import org.bukkit.entity.Player
import java.util.UUID

@Service
object HousingMusicService
{
    private val songs: MutableMap<String, HousingMusic> = mutableMapOf()
    private val songPlayers: MutableMap<String, RadioSongPlayer> = mutableMapOf()
    private val songTracker: MutableMap<UUID, String> = mutableMapOf()

    @Configure
    fun configure()
    {
        val files = PersistentGameHousing.instance.dataFolder.resolve("nbs")

        if (!files.isDirectory) return

        files.listFiles().forEach { file ->
            val name = file.nameWithoutExtension

            songs[name] = HousingMusic(name, file)
        }
    }

    fun listSongs() = songs.values.toList()

    fun playSong(name: String, player: Player)
    {
        val song = songs[name]
            ?: return

        val songPlayer = songPlayers[name]
            ?: RadioSongPlayer(NBSDecoder.parse(song.file))

        // make sure tracks don't overlap here
        if (isPlayingSong(player))
        {
            stopPlayingSong(player)
        }

        songPlayer.isPlaying = true
        songPlayer.addPlayer(player)
        songTracker[player.uniqueId] = name
    }

    fun isPlayingSong(player: Player) = songTracker.contains(player.uniqueId)

    fun stopPlayingSong(player: Player)
    {
        val songName = songTracker[player.uniqueId]
            ?: return

        val currentRadio = songPlayers[songName]
            ?: return

        currentRadio.removePlayer(player)
        songTracker.remove(player.uniqueId)
    }
}