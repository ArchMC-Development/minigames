package gg.tropic.practice.minigame

import gg.scala.commons.annotations.plugin.SoftDependency
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan
import gg.tropic.practice.configuration.PracticeConfigurationService
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 6/29/25
 */
@Service
@IgnoreAutoScan
@SoftDependency("PlaceholderAPI")
object MinigameLobbyPlaceholderService : PlaceholderExpansion()
{
    override fun getIdentifier(): String = "minigames"
    override fun getAuthor(): String = "ArchMC"
    override fun getVersion(): String = "1.0.0"

    @Configure
    fun configure()
    {
        register()
    }

    override fun onPlaceholderRequest(player: Player?, params: String): String
    {
        val split = params.split("-")
        if (split.first() == "playing")
        {
            val mode = split.getOrNull(1)
                ?: return "0 playing"

            val modeMetadata = PracticeConfigurationService
                .minigameType()
                .provide()
                .modeNullable(mode)
                ?: return "0 playing"

            return "${modeMetadata.playersPlaying()} playing"
        }

        return ""
    }
}
