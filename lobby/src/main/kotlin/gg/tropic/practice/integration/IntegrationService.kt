package gg.tropic.practice.integration

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.tropic.practice.configuration.PracticeConfigurationService
import org.bukkit.Bukkit

/**
 * Class created on 2/21/2026

 * @author Max C.
 * @project arch-minigames
 * @website https://solo.to/redis
 */
@Service
object IntegrationService
{
    @Configure
    fun configure()
    {
        val config = PracticeConfigurationService.local()
        val location = config.rankGiftLeaderboardLocation

        if (location != null)
        {
            val world = Bukkit.getWorlds().first()
            val hologram = RankGiftLeaderboardHologram(location.toLocation(world))

            hologram.configure()
        }
    }
}
