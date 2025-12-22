package gg.tropic.practice.privategames

import gg.tropic.practice.privategames.settings.PrivateGameSetting
import gg.tropic.practice.privategames.settings.PrivateGameSettingsRegistry

/**
 * Settings container for Private Games.
 * Uses the extensible PrivateGameSetting interface for game-specific settings.
 *
 * @author GrowlyX
 * @since 12/21/24
 */
data class PrivateGameSettings(
    val trackStats: Boolean = false,
    val teamSelectorEnabled: Boolean = true,
    
    // Generic settings applicable to all games
    var maxPlayersPerTeam: Int? = null,
    var friendlyFire: Boolean = false,
    
    // Game-specific settings loaded dynamically
    val gameSpecificSettings: MutableMap<String, Any> = mutableMapOf()
) {
    companion object {
        fun default() = PrivateGameSettings()
    }

    /**
     * Load game-specific settings for the given game type.
     * Returns fresh setting instances from the registry.
     */
    fun loadSettingsForGameType(gameType: String): List<PrivateGameSetting<*>>
    {
        return PrivateGameSettingsRegistry.getSettingsFor(gameType)
    }

    /**
     * Get a setting value by ID, with fallback to default.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getSetting(id: String, default: T): T
    {
        return gameSpecificSettings[id] as? T ?: default
    }

    /**
     * Set a setting value by ID.
     */
    fun setSetting(id: String, value: Any)
    {
        gameSpecificSettings[id] = value
    }
}
