package mc.arch.minigames.persistent.housing.game.music

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import mc.arch.minigames.persistent.housing.game.PersistentGameHousing

@Service
object HousingMusicService
{
    private val songs: MutableMap<String, HousingMusic> = mutableMapOf()

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
}