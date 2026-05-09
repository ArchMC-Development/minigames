package mc.arch.minigames.duelsmodern.lobby

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.tropic.practice.configuration.MinigameLobbyConfiguration
import gg.tropic.practice.configuration.PracticeConfigurationService
import gg.tropic.practice.minigame.MiniGameTypeProvider
import mc.arch.minigames.duelsmodern.DuelsModernTypeMetadata

@Service
object DuelsModernLobbyCustomizer : MiniGameTypeProvider
{
    @Configure
    fun configure()
    {
        PracticeConfigurationService.registerTypeProvider(this)

        with(PracticeConfigurationService.cached()) {
            if (!minigameConfigurations.containsKey(provide().internalId))
            {
                minigameConfigurations[provide().internalId] = MinigameLobbyConfiguration()
                PracticeConfigurationService.sync(this)
            }
        }
    }

    override fun provide() = DuelsModernTypeMetadata
}
