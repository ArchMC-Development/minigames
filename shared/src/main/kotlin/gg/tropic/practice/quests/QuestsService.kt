package gg.tropic.practice.quests

import gg.scala.basics.plugin.profile.BasicsProfileService
import gg.scala.basics.plugin.settings.defaults.values.StateSettingValue
import gg.scala.flavor.service.Service
import gg.tropic.practice.configuration.PracticeConfigurationService
import gg.tropic.practice.quests.model.Quest
import gg.tropic.practice.settings.DuelsSettingCategory
import gg.tropic.practice.statistics.StatisticLifetime
import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 7/8/25
 */
@Service
object QuestsService
{
    fun getActiveMinigameQuests(minigameID: String) = (PracticeConfigurationService
        .cached()
        .minigameConfigurations[minigameID]
        ?.quests ?: mapOf())
        .filterValues { it.active == true }
        .toMap()

    fun getActiveMinigameQuestsOfLifetime(minigameID: String, lifetime: StatisticLifetime) = (PracticeConfigurationService
        .cached()
        .minigameConfigurations[minigameID]
        ?.quests ?: mapOf())
        .filterValues { it.active == true && it.lifetime == lifetime }
        .values
        .toList()
        .take(4)

    fun getLimitedActiveMinigameQuests(minigameID: String) = getActiveMinigameQuestsOfLifetime(minigameID, StatisticLifetime.Daily) +
        getActiveMinigameQuestsOfLifetime(minigameID, StatisticLifetime.Weekly)

    fun isAutoAccepting(player: Player): Boolean
    {
        val profile = BasicsProfileService.find(player)
            ?: return false

        val messagesRef = profile.settings["${DuelsSettingCategory.DUEL_SETTING_PREFIX}:auto-accept-quests"]!!
        val autoAccepting = messagesRef.map<StateSettingValue>()

        return autoAccepting == StateSettingValue.ENABLED && player.hasPermission("minigame.quests.autoactivate")
    }
}
